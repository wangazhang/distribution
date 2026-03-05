#!/bin/bash

# MB模块分销功能自动化测试执行脚本
#
# 使用方法:
# ./run-tests.sh [test-type] [options]
#
# test-type:
#   unit      - 只运行单元测试 (默认)
#   all       - 运行所有测试
#   specific  - 运行指定测试类
#
# options:
#   --coverage      - 生成代码覆盖率报告
#   --verbose       - 详细输出
#   --clean         - 清理之前的测试结果

set -e

# 默认参数
TEST_TYPE="unit"
ENABLE_COVERAGE=false
VERBOSE=false
CLEAN=false
SPECIFIC_TEST=""

# 项目根目录
PROJECT_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/../../../.." && pwd)"
MODULE_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/../../../" && pwd)"

# 解析命令行参数
while [[ $# -gt 0 ]]; do
    case $1 in
        unit|all|specific)
            TEST_TYPE="$1"
            shift
            ;;
        --coverage)
            ENABLE_COVERAGE=true
            shift
            ;;
        --verbose)
            VERBOSE=true
            shift
            ;;
        --clean)
            CLEAN=true
            shift
            ;;
        --test)
            if [[ -n "$2" ]]; then
                SPECIFIC_TEST="$2"
                TEST_TYPE="specific"
                shift 2
            else
                echo "错误: --test 需要指定测试类名"
                exit 1
            fi
            ;;
        -h|--help)
            echo "MB模块分销功能自动化测试执行脚本"
            echo ""
            echo "使用方法: $0 [test-type] [options]"
            echo ""
            echo "test-type:"
            echo "  unit        只运行单元测试 (默认)"
            echo "  all         运行所有测试"
            echo "  specific    运行指定测试类"
            echo ""
            echo "options:"
            echo "  --coverage      生成代码覆盖率报告"
            echo "  --verbose       详细输出"
            echo "  --clean         清理之前的测试结果"
            echo "  --test <name>   指定要运行的测试类名"
            echo "  -h, --help      显示帮助信息"
            echo ""
            echo "示例:"
            echo "  $0 unit --coverage"
            echo "  $0 --test SimpleTest"
            echo "  $0 --test MaterialApiImplTest --verbose"
            exit 0
            ;;
        *)
            echo "未知参数: $1"
            echo "使用 $0 --help 查看帮助信息"
            exit 1
            ;;
    esac
done

# 颜色输出
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# 日志函数
log_info() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

log_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

log_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

log_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# 检查环境
check_environment() {
    log_info "检查测试环境..."
    
    # 检查Java版本
    if ! command -v java &> /dev/null; then
        log_error "Java未安装或不在PATH中"
        exit 1
    fi
    
    # 检查Maven
    if ! command -v mvn &> /dev/null; then
        log_error "Maven未安装或不在PATH中"
        exit 1
    fi
    
    # 检查项目结构
    if [[ ! -f "$PROJECT_ROOT/pom.xml" ]]; then
        log_error "项目根目录pom.xml不存在: $PROJECT_ROOT"
        exit 1
    fi
    
    log_success "环境检查通过"
}

# 清理测试结果
clean_test_results() {
    if [[ "$CLEAN" == true ]]; then
        log_info "清理之前的测试结果..."
        cd "$PROJECT_ROOT"
        mvn clean -q
        rm -rf "$MODULE_ROOT/target/surefire-reports"
        rm -rf "$MODULE_ROOT/target/site/jacoco"
        log_success "清理完成"
    fi
}

# 构建Maven命令
build_maven_command() {
    local cmd="mvn test"
    
    # 添加配置文件
    cmd="$cmd -Dspring.profiles.active=unit-test"
    
    # 真实服务测试
    if [[ "$ENABLE_REAL_SERVICE" == true ]]; then
        cmd="$cmd -Dtest.real-service.enabled=true"
        log_warning "启用真实服务测试，请确保服务可用"
    fi
    
    # 代码覆盖率
    if [[ "$ENABLE_COVERAGE" == true ]]; then
        cmd="$cmd -Djacoco.skip=false"
    fi
    
    # 并行执行
    if [[ "$ENABLE_PARALLEL" == true ]]; then
        cmd="$cmd -Djunit.jupiter.execution.parallel.enabled=true"
        cmd="$cmd -Djunit.jupiter.execution.parallel.mode.default=concurrent"
    fi
    
    # 详细输出
    if [[ "$VERBOSE" == true ]]; then
        cmd="$cmd -X"
    else
        cmd="$cmd -q"
    fi
    
    echo "$cmd"
}

# 运行单元测试
run_unit_tests() {
    log_info "运行单元测试..."
    cd "$MODULE_ROOT"
    
    local cmd=$(build_maven_command)
    cmd="$cmd -Dtest=**/*Test"
    cmd="$cmd -DexcludedGroups=integration"
    
    if eval "$cmd"; then
        log_success "单元测试执行成功"
        return 0
    else
        log_error "单元测试执行失败"
        return 1
    fi
}

# 运行集成测试
run_integration_tests() {
    log_info "运行集成测试..."
    cd "$MODULE_ROOT"
    
    local cmd=$(build_maven_command)
    cmd="$cmd -Dtest=**/*IntegrationTest"
    cmd="$cmd -Dgroups=integration"
    
    if eval "$cmd"; then
        log_success "集成测试执行成功"
        return 0
    else
        log_error "集成测试执行失败"
        return 1
    fi
}

# 运行所有测试
run_all_tests() {
    log_info "运行所有测试..."
    cd "$MODULE_ROOT"
    
    local cmd=$(build_maven_command)
    
    if eval "$cmd"; then
        log_success "所有测试执行成功"
        return 0
    else
        log_error "测试执行失败"
        return 1
    fi
}

# 运行指定测试
run_specific_test() {
    log_info "运行指定测试: $SPECIFIC_TEST"
    cd "$MODULE_ROOT"

    local cmd=$(build_maven_command)
    cmd="$cmd -Dtest=$SPECIFIC_TEST"

    if eval "$cmd"; then
        log_success "测试 $SPECIFIC_TEST 执行成功"
        return 0
    else
        log_error "测试 $SPECIFIC_TEST 执行失败"
        return 1
    fi
}

# 运行测试套件
run_test_suite() {
    log_info "运行测试套件..."
    cd "$MODULE_ROOT"

    local cmd=$(build_maven_command)
    cmd="$cmd -Dtest=MbDistributionTestSuite"

    if eval "$cmd"; then
        log_success "测试套件执行成功"
        return 0
    else
        log_error "测试套件执行失败"
        return 1
    fi
}

# 生成测试报告
generate_reports() {
    log_info "生成测试报告..."
    cd "$MODULE_ROOT"
    
    # 生成Surefire报告
    if [[ -d "target/surefire-reports" ]]; then
        log_info "Surefire测试报告: $MODULE_ROOT/target/surefire-reports/"
    fi
    
    # 生成代码覆盖率报告
    if [[ "$ENABLE_COVERAGE" == true ]]; then
        mvn jacoco:report -q
        if [[ -d "target/site/jacoco" ]]; then
            log_success "代码覆盖率报告: $MODULE_ROOT/target/site/jacoco/index.html"
        fi
    fi
}

# 主函数
main() {
    log_info "开始执行MB模块分销功能自动化测试"
    log_info "测试类型: $TEST_TYPE"
    log_info "项目根目录: $PROJECT_ROOT"
    log_info "模块目录: $MODULE_ROOT"
    
    check_environment
    clean_test_results
    
    local exit_code=0
    
    case "$TEST_TYPE" in
        unit)
            run_unit_tests || exit_code=$?
            ;;
        all)
            run_all_tests || exit_code=$?
            ;;
        specific)
            if [[ -z "$SPECIFIC_TEST" ]]; then
                log_error "指定测试模式需要提供测试类名"
                exit 1
            fi
            run_specific_test || exit_code=$?
            ;;
    esac
    
    generate_reports
    
    if [[ $exit_code -eq 0 ]]; then
        log_success "测试执行完成"
    else
        log_error "测试执行失败，退出码: $exit_code"
    fi
    
    exit $exit_code
}

# 执行主函数
main "$@"
