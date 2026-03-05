#!/bin/bash

# RestockCommissionHandler 集成测试执行脚本
# 使用真实数据库进行完整的分佣流程测试

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# 脚本配置
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_DIR="$(cd "$SCRIPT_DIR/../../../.." && pwd)"
TEST_PROFILE="integration-test"
MAVEN_OPTS="-Xmx2g -XX:+UseG1GC"

# 数据库配置
DB_HOST="${DB_HOST:-localhost}"
DB_PORT="${DB_PORT:-3306}"
DB_NAME="${DB_NAME:-distribution_test}"
DB_USERNAME="${DB_USERNAME:-root}"
DB_PASSWORD="${DB_PASSWORD:-123456}"

# Redis配置
REDIS_HOST="${REDIS_HOST:-127.0.0.1}"
REDIS_PORT="${REDIS_PORT:-6379}"
REDIS_DATABASE="${REDIS_DATABASE:-1}"

echo -e "${BLUE}==================== RestockCommissionHandler 集成测试 ====================${NC}"
echo -e "${YELLOW}测试配置:${NC}"
echo -e "  数据库: ${DB_HOST}:${DB_PORT}/${DB_NAME}"
echo -e "  Redis: ${REDIS_HOST}:${REDIS_PORT}/${REDIS_DATABASE}"
echo -e "  测试配置: ${TEST_PROFILE}"
echo -e "  项目目录: ${PROJECT_DIR}"
echo ""

# 检查依赖
check_dependencies() {
    echo -e "${YELLOW}检查依赖...${NC}"
    
    # 检查Maven
    if ! command -v mvn &> /dev/null; then
        echo -e "${RED}错误: Maven 未安装${NC}"
        exit 1
    fi
    
    # 检查MySQL连接
    if command -v mysql &> /dev/null; then
        if ! mysql -h"$DB_HOST" -P"$DB_PORT" -u"$DB_USERNAME" -p"$DB_PASSWORD" -e "SELECT 1;" &> /dev/null; then
            echo -e "${RED}警告: 无法连接到MySQL数据库${NC}"
            echo -e "${YELLOW}请确保数据库服务正在运行并且连接参数正确${NC}"
        else
            echo -e "${GREEN}✓ MySQL连接正常${NC}"
        fi
    fi
    
    # 检查Redis连接
    if command -v redis-cli &> /dev/null; then
        if ! redis-cli -h "$REDIS_HOST" -p "$REDIS_PORT" ping &> /dev/null; then
            echo -e "${RED}警告: 无法连接到Redis服务${NC}"
            echo -e "${YELLOW}请确保Redis服务正在运行${NC}"
        else
            echo -e "${GREEN}✓ Redis连接正常${NC}"
        fi
    fi
    
    echo ""
}

# 准备测试环境
prepare_test_environment() {
    echo -e "${YELLOW}准备测试环境...${NC}"
    
    # 设置环境变量
    export SPRING_PROFILES_ACTIVE="$TEST_PROFILE"
    export DB_USERNAME="$DB_USERNAME"
    export DB_PASSWORD="$DB_PASSWORD"
    export REDIS_HOST="$REDIS_HOST"
    export REDIS_PORT="$REDIS_PORT"
    export MAVEN_OPTS="$MAVEN_OPTS"
    
    # 创建测试数据库（如果不存在）
    if command -v mysql &> /dev/null; then
        mysql -h"$DB_HOST" -P"$DB_PORT" -u"$DB_USERNAME" -p"$DB_PASSWORD" -e "CREATE DATABASE IF NOT EXISTS $DB_NAME;" 2>/dev/null || true
    fi
    
    echo -e "${GREEN}✓ 测试环境准备完成${NC}"
    echo ""
}

# 清理测试数据
clean_test_data() {
    echo -e "${YELLOW}清理测试数据...${NC}"
    
    cd "$PROJECT_DIR"
    
    # 执行数据清理
    mvn test-compile exec:java \
        -Dexec.mainClass="com.hissp.distribution.module.mb.test.TestDataCleaner" \
        -Dexec.classpathScope="test" \
        -Dspring.profiles.active="$TEST_PROFILE" \
        -q 2>/dev/null || echo -e "${YELLOW}数据清理完成（可能没有数据需要清理）${NC}"
    
    echo -e "${GREEN}✓ 测试数据清理完成${NC}"
    echo ""
}

# 执行集成测试
run_integration_tests() {
    echo -e "${YELLOW}执行集成测试...${NC}"
    
    cd "$PROJECT_DIR"
    
    local test_class="RestockCommissionHandlerIntegrationTest"
    local start_time=$(date +%s)
    
    # 执行测试
    mvn test \
        -Dtest="$test_class" \
        -Dspring.profiles.active="$TEST_PROFILE" \
        -DDB_USERNAME="$DB_USERNAME" \
        -DDB_PASSWORD="$DB_PASSWORD" \
        -DREDIS_HOST="$REDIS_HOST" \
        -DREDIS_PORT="$REDIS_PORT" \
        -Dmaven.test.failure.ignore=false
    
    local exit_code=$?
    local end_time=$(date +%s)
    local duration=$((end_time - start_time))
    
    echo ""
    if [ $exit_code -eq 0 ]; then
        echo -e "${GREEN}✓ 集成测试执行成功 (耗时: ${duration}秒)${NC}"
    else
        echo -e "${RED}✗ 集成测试执行失败 (耗时: ${duration}秒)${NC}"
    fi
    
    return $exit_code
}

# 生成测试报告
generate_test_report() {
    echo -e "${YELLOW}生成测试报告...${NC}"
    
    cd "$PROJECT_DIR"
    
    local report_dir="target/integration-test-reports"
    mkdir -p "$report_dir"
    
    # 复制Surefire报告
    if [ -d "target/surefire-reports" ]; then
        cp -r target/surefire-reports/* "$report_dir/" 2>/dev/null || true
    fi
    
    # 生成简单的HTML报告
    cat > "$report_dir/integration-test-summary.html" << EOF
<!DOCTYPE html>
<html>
<head>
    <title>RestockCommissionHandler 集成测试报告</title>
    <meta charset="UTF-8">
    <style>
        body { font-family: Arial, sans-serif; margin: 20px; }
        .header { background-color: #f0f0f0; padding: 10px; border-radius: 5px; }
        .success { color: green; }
        .failure { color: red; }
        .info { color: blue; }
        table { border-collapse: collapse; width: 100%; margin-top: 20px; }
        th, td { border: 1px solid #ddd; padding: 8px; text-align: left; }
        th { background-color: #f2f2f2; }
    </style>
</head>
<body>
    <div class="header">
        <h1>RestockCommissionHandler 集成测试报告</h1>
        <p>生成时间: $(date)</p>
        <p>测试环境: ${TEST_PROFILE}</p>
        <p>数据库: ${DB_HOST}:${DB_PORT}/${DB_NAME}</p>
    </div>
    
    <h2>测试配置</h2>
    <table>
        <tr><th>配置项</th><th>值</th></tr>
        <tr><td>测试配置文件</td><td>${TEST_PROFILE}</td></tr>
        <tr><td>数据库连接</td><td>${DB_HOST}:${DB_PORT}/${DB_NAME}</td></tr>
        <tr><td>Redis连接</td><td>${REDIS_HOST}:${REDIS_PORT}/${REDIS_DATABASE}</td></tr>
        <tr><td>Maven选项</td><td>${MAVEN_OPTS}</td></tr>
    </table>
    
    <h2>测试结果</h2>
    <p>详细的测试结果请查看 surefire-reports 目录中的文件。</p>
    
    <h2>测试说明</h2>
    <ul>
        <li>本测试使用真实数据库进行集成测试</li>
        <li>测试数据会在测试前后自动清理</li>
        <li>测试覆盖了完整的分佣业务流程</li>
        <li>包含边界条件和异常场景测试</li>
    </ul>
</body>
</html>
EOF
    
    echo -e "${GREEN}✓ 测试报告生成完成: $report_dir${NC}"
    echo ""
}

# 显示帮助信息
show_help() {
    echo "RestockCommissionHandler 集成测试执行脚本"
    echo ""
    echo "用法: $0 [选项]"
    echo ""
    echo "选项:"
    echo "  -h, --help              显示帮助信息"
    echo "  -c, --clean-only        仅清理测试数据，不执行测试"
    echo "  -r, --report-only       仅生成测试报告"
    echo "  -s, --skip-cleanup      跳过测试后的数据清理"
    echo "  --db-host HOST          数据库主机 (默认: localhost)"
    echo "  --db-port PORT          数据库端口 (默认: 3306)"
    echo "  --db-name NAME          数据库名称 (默认: distribution_test)"
    echo "  --db-user USER          数据库用户名 (默认: root)"
    echo "  --db-password PASS      数据库密码 (默认: 123456)"
    echo "  --redis-host HOST       Redis主机 (默认: 127.0.0.1)"
    echo "  --redis-port PORT       Redis端口 (默认: 6379)"
    echo ""
    echo "示例:"
    echo "  $0                      执行完整的集成测试"
    echo "  $0 --clean-only         仅清理测试数据"
    echo "  $0 --skip-cleanup       执行测试但不清理数据"
    echo "  $0 --db-host mysql.example.com --db-user testuser"
    echo ""
}

# 主函数
main() {
    local clean_only=false
    local report_only=false
    local skip_cleanup=false
    
    # 解析命令行参数
    while [[ $# -gt 0 ]]; do
        case $1 in
            -h|--help)
                show_help
                exit 0
                ;;
            -c|--clean-only)
                clean_only=true
                shift
                ;;
            -r|--report-only)
                report_only=true
                shift
                ;;
            -s|--skip-cleanup)
                skip_cleanup=true
                shift
                ;;
            --db-host)
                DB_HOST="$2"
                shift 2
                ;;
            --db-port)
                DB_PORT="$2"
                shift 2
                ;;
            --db-name)
                DB_NAME="$2"
                shift 2
                ;;
            --db-user)
                DB_USERNAME="$2"
                shift 2
                ;;
            --db-password)
                DB_PASSWORD="$2"
                shift 2
                ;;
            --redis-host)
                REDIS_HOST="$2"
                shift 2
                ;;
            --redis-port)
                REDIS_PORT="$2"
                shift 2
                ;;
            *)
                echo -e "${RED}未知选项: $1${NC}"
                show_help
                exit 1
                ;;
        esac
    done
    
    # 执行相应的操作
    if [ "$report_only" = true ]; then
        generate_test_report
        exit 0
    fi
    
    if [ "$clean_only" = true ]; then
        check_dependencies
        prepare_test_environment
        clean_test_data
        exit 0
    fi
    
    # 执行完整的测试流程
    check_dependencies
    prepare_test_environment
    clean_test_data
    
    if run_integration_tests; then
        generate_test_report
        
        if [ "$skip_cleanup" != true ]; then
            clean_test_data
        else
            echo -e "${YELLOW}跳过测试后数据清理（--skip-cleanup）${NC}"
        fi
        
        echo -e "${GREEN}==================== 集成测试完成 ====================${NC}"
        exit 0
    else
        echo -e "${RED}==================== 集成测试失败 ====================${NC}"
        exit 1
    fi
}

# 执行主函数
main "$@"
