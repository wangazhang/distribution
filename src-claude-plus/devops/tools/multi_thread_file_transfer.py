#!/usr/bin/env python3
# -*- coding: utf-8 -*-

import os
import sys
import argparse
import threading
import queue
import time
import paramiko
from concurrent.futures import ThreadPoolExecutor
from tqdm import tqdm


class FileTransfer:
    def __init__(self, host, port, username, password=None, key_file=None, 
                 max_workers=5, remote_dir='/tmp', log=True):
        """
        初始化文件传输类
        
        参数:
            host (str): 远程服务器主机名或IP
            port (int): SSH端口号
            username (str): SSH用户名
            password (str, optional): SSH密码
            key_file (str, optional): SSH私钥文件路径
            max_workers (int): 最大工作线程数
            remote_dir (str): 远程目标目录
            log (bool): 是否显示进度条
        """
        self.host = host
        self.port = port
        self.username = username
        self.password = password
        self.key_file = key_file
        self.max_workers = max_workers
        self.remote_dir = remote_dir
        self.log = log
        self.task_queue = queue.Queue()
        self.total_files = 0
        self.transferred_files = 0
        self.lock = threading.Lock()
        self.progress_bar = None
        
    def connect(self):
        """创建SSH客户端连接"""
        client = paramiko.SSHClient()
        client.set_missing_host_key_policy(paramiko.AutoAddPolicy())
        
        try:
            if self.key_file:
                key = paramiko.RSAKey.from_private_key_file(self.key_file)
                client.connect(self.host, port=self.port, username=self.username, pkey=key)
            else:
                client.connect(self.host, port=self.port, username=self.username, password=self.password)
            return client
        except Exception as e:
            print(f"连接失败: {str(e)}")
            return None
    
    def transfer_file(self, local_path, remote_path):
        """
        传输单个文件到远程服务器
        
        参数:
            local_path (str): 本地文件路径
            remote_path (str): 远程文件路径
        """
        try:
            client = self.connect()
            if not client:
                return False
            
            sftp = client.open_sftp()
            
            # 确保远程目录存在
            remote_dir = os.path.dirname(remote_path)
            try:
                sftp.stat(remote_dir)
            except IOError:
                # 递归创建目录
                dirs = remote_dir.split('/')
                current_dir = ''
                for d in dirs:
                    if not d:
                        continue
                    current_dir += '/' + d
                    try:
                        sftp.stat(current_dir)
                    except IOError:
                        sftp.mkdir(current_dir)
            
            # 传输文件
            sftp.put(local_path, remote_path)
            sftp.close()
            client.close()
            
            with self.lock:
                self.transferred_files += 1
                if self.progress_bar:
                    self.progress_bar.update(1)
            
            return True
        except Exception as e:
            print(f"传输文件 {local_path} 失败: {str(e)}")
            return False
    
    def worker(self):
        """工作线程函数，从队列中获取任务并执行"""
        while True:
            try:
                task = self.task_queue.get(block=False)
                if task is None:
                    break
                
                local_path, remote_path = task
                self.transfer_file(local_path, remote_path)
                
                self.task_queue.task_done()
            except queue.Empty:
                break
    
    def add_file(self, local_path, remote_path=None):
        """
        添加要传输的文件到队列
        
        参数:
            local_path (str): 本地文件路径
            remote_path (str, optional): 远程文件路径，如果不指定则使用与本地相同的文件名
        """
        if not os.path.isfile(local_path):
            print(f"文件不存在: {local_path}")
            return
        
        if remote_path is None:
            filename = os.path.basename(local_path)
            remote_path = os.path.join(self.remote_dir, filename)
        
        self.task_queue.put((local_path, remote_path))
        self.total_files += 1
    
    def add_directory(self, local_dir, preserve_path=False):
        """
        添加整个目录下的文件到队列
        
        参数:
            local_dir (str): 本地目录路径
            preserve_path (bool): 是否保留相对路径结构
        """
        if not os.path.isdir(local_dir):
            print(f"目录不存在: {local_dir}")
            return
        
        for root, _, files in os.walk(local_dir):
            for filename in files:
                local_path = os.path.join(root, filename)
                
                if preserve_path:
                    rel_path = os.path.relpath(local_path, local_dir)
                    remote_path = os.path.join(self.remote_dir, rel_path)
                else:
                    remote_path = os.path.join(self.remote_dir, filename)
                
                self.task_queue.put((local_path, remote_path))
                self.total_files += 1
    
    def start(self):
        """开始传输所有文件"""
        if self.total_files == 0:
            print("没有文件需要传输")
            return
        
        if self.log:
            print(f"开始传输 {self.total_files} 个文件到 {self.host}:{self.remote_dir}")
            self.progress_bar = tqdm(total=self.total_files, desc="传输进度")
        
        start_time = time.time()
        
        # 创建线程池
        with ThreadPoolExecutor(max_workers=self.max_workers) as executor:
            threads = []
            for _ in range(self.max_workers):
                threads.append(executor.submit(self.worker))
        
        # 等待所有任务完成
        self.task_queue.join()
        
        # 结束所有线程
        for _ in range(self.max_workers):
            self.task_queue.put(None)
        
        end_time = time.time()
        
        if self.progress_bar:
            self.progress_bar.close()
        
        print(f"传输完成! 总共传输 {self.transferred_files}/{self.total_files} 个文件")
        print(f"耗时: {end_time - start_time:.2f} 秒")


def main():
    parser = argparse.ArgumentParser(description="多线程文件传输工具")
    parser.add_argument("-H", "--host", required=True, help="远程服务器地址")
    parser.add_argument("-P", "--port", type=int, default=22, help="SSH端口号，默认为22")
    parser.add_argument("-u", "--username", required=True, help="SSH用户名")
    parser.add_argument("-p", "--password", help="SSH密码")
    parser.add_argument("-k", "--key-file", help="SSH私钥文件路径")
    parser.add_argument("-r", "--remote-dir", default="/tmp", help="远程目标目录，默认为/tmp")
    parser.add_argument("-w", "--workers", type=int, default=5, help="工作线程数，默认为5")
    parser.add_argument("-f", "--files", nargs="+", help="要传输的文件路径列表")
    parser.add_argument("-d", "--dirs", nargs="+", help="要传输的目录路径列表")
    parser.add_argument("--preserve-path", action="store_true", help="保留相对路径结构")
    
    args = parser.parse_args()
    
    if not args.files and not args.dirs:
        parser.error("至少需要指定一个文件(-f)或目录(-d)")
    
    if not args.password and not args.key_file:
        parser.error("需要提供密码(-p)或私钥文件(-k)")
    
    # 初始化传输器
    transfer = FileTransfer(
        host=args.host,
        port=args.port,
        username=args.username,
        password=args.password,
        key_file=args.key_file,
        max_workers=args.workers,
        remote_dir=args.remote_dir
    )
    
    # 添加文件
    if args.files:
        for file_path in args.files:
            transfer.add_file(file_path)
    
    # 添加目录
    if args.dirs:
        for dir_path in args.dirs:
            transfer.add_directory(dir_path, args.preserve_path)
    
    # 开始传输
    transfer.start()


if __name__ == "__main__":
    main() 