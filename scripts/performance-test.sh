#!/bin/zsh

# 1. 환경 변수 및 경로 설정
PROJECT_ROOT=$(pwd)
TEST_DIR="$PROJECT_ROOT/performance-test"
RESULT_DIR="$TEST_DIR/results"
TIMESTAMP=$(date +%Y%m%d_%H%M%S)
RESULT_FILE="$RESULT_DIR/result_$TIMESTAMP.json"

# 결과 저장 폴더 생성
mkdir -p "$RESULT_DIR"

echo "🚀 [Step 1] 성능 테스트 자동화 파이프라인 시작"

# 2. k6 스크립트 존재 여부 확인 (Scenario Agent가 생성한 파일)
if [ ! -f "$TEST_DIR/load-test.js" ]; then
    echo "❌ 에러: k6 스크립트(load-test.js)를 찾을 수 없습니다."
    echo "💡 먼저 Scenario Agent에게 'load-test.js를 생성해줘'라고 명령하세요."
    exit 1
fi

# 3. k6 부하 테스트 실행
echo "📊 [Step 2] k6 부하 테스트 실행 중..."
# stdout은 화면에 출력하고, 결과는 JSON으로 별도 저장
k6 run --out json="$RESULT_FILE" "$TEST_DIR/load-test.js"

if [ $? -eq 0 ]; then
    echo "✅ [Step 3] 테스트 완료! 결과 파일: $RESULT_FILE"
else
    echo "❌ [Step 3] 테스트 실행 중 오류가 발생했습니다."
    exit 1
fi

# 4. AI Profiler Agent에게 전달하기 위한 준비
echo "🤖 [Step 4] Profiler Agent 분석 준비..."
echo "-------------------------------------------------------"
echo "Claude(또는 AI)에게 아래 명령어를 입력하여 분석을 시작하세요:"
echo ""
echo "클로드, '$RESULT_FILE' 파일을 읽고 'Profiler Agent' 페르소나로"
echo "병목 지점 분석 및 최적화 리포트를 작성해줘."
echo "-------------------------------------------------------"