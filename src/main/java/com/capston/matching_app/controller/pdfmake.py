from fpdf import FPDF

class PDF(FPDF):
    def header(self):
        self.set_font("Arial", 'B', 16)
        self.set_text_color(0, 70, 140)
        self.cell(0, 10, "AI 기반 이상형 매칭 서비스 – 백엔드 포트폴리오", 0, 1, 'C')
        self.ln(5)

    def chapter_title(self, num, label):
        self.set_font("Arial", 'B', 12)
        self.set_text_color(255, 255, 255)
        self.set_fill_color(0, 70, 140)
        self.cell(0, 10, f"{num}. {label}", 0, 1, 'L', 1)
        self.ln(2)

    def chapter_body(self, body):
        self.set_font("Arial", '', 11)
        self.set_text_color(0, 0, 0)
        self.multi_cell(0, 7, body)
        self.ln(3)

    def code_box(self, text):
        self.set_font("Arial", '', 11)
        self.set_fill_color(240, 240, 240)
        self.multi_cell(0, 6, text, 0, 1, '', 1)
        self.ln(3)

pdf = PDF()
pdf.add_page()

# 1. 프로젝트 개요
pdf.chapter_title(1, "프로젝트 개요")
pdf.chapter_body(
    """이 프로젝트는 외모 기반의 AI 추천과 강력한 본인 인증 시스템을 결합한 새로운 개념의 매칭 서비스입니다. 기존의 설문 기반 매칭 서비스가 가지는 주관성의 한계를 극복하고, 사진 도용 문제를 해결하여 사용자들에게 신뢰할 수 있는 만남 환경을 제공하는 것을 목표로 했습니다.
    
    핵심 기능:
    
    - AI 기반 이상형 추천: 사용자가 업로드한 이상형 사진을 분석하여 가장 유사한 외모를 가진 사용자를 추천합니다.
    - 강화된 본인 인증: 정면, 좌/우측의 다각도 이미지를 활용하여 프로필 사진과 실시간 얼굴이 일치하는지 판별합니다.
    - 1:1 매칭 시스템: 한 번에 오직 한 사람과만 매칭을 진행하여, 진지하고 신중한 만남을 유도합니다.
    """
)

# 2. 백엔드 기술 스택 및 역할
pdf.chapter_title(2, "백엔드 기술 스택 및 역할")
pdf.chapter_body(
    """프로젝트의 핵심 로직을 담당하며, 다음과 같은 기술 스택을 활용했습니다.
    
    백엔드: Spring Boot, Java
    AI 서버: Flask, Python
    데이터베이스: MySQL
    """
)
pdf.code_box(
    """주요 역할:
    - API 설계 및 구현: AI 서버(Flask) 및 Android 클라이언트와 통신하기 위한 RESTful API를 설계하고 구현했습니다.
    - 매칭 로직 관리: 사용자의 매칭 요청 및 상태(대기, 수락, 거절)를 관리하는 핵심 로직을 개발했습니다.
    - 데이터베이스 관리: 사용자 프로필, 매칭 기록, 미션, 신고 내역 등을 저장하고 관리하는 데이터베이스(MySQL)를 설계하고 구축했습니다.
    """
)

# 3. 기술적 도전과 해결 과정
pdf.chapter_title(3, "기술적 도전과 해결 과정")
pdf.chapter_body(
    """프로젝트를 진행하며 직면했던 주요 기술적 난관과 이를 해결한 과정은 다음과 같습니다.
    
    3.1 대용량 파일 전송 방식 개선
    문제: 초기에는 이미지를 String 형태로 인코딩하여 주고받으려고 했으나, 데이터 용량이 커지면서 통신 속도가 느려지고 서버에 과도한 부하가 발생했습니다.
    해결: 대용량 파일 전송에 최적화된 Multipart/form-data 방식을 도입했습니다. Android 클라이언트에서 이미지를 MultipartFile로 변환해 전송하고, Spring Boot 서버에서 이를 효율적으로 처리하도록 구현하여 성능을 획기적으로 개선했습니다.
    
    3.2 이기종 서버 간 연동 (Spring Boot & Flask)
    문제: 백엔드와 AI 서버가 각각 다른 언어(Java, Python)와 프레임워크(Spring, Flask)를 사용했기 때문에, 데이터 통신과 연동 방식에 대한 명확한 설계가 필요했습니다.
    해결: RESTful API를 중심으로 연동했습니다. Spring Boot에서는 WebClient를 이용해 Flask AI 서버의 API를 호출하고, AI 모델이 반환하는 얼굴 임베딩 벡터와 같은 경량화된 데이터만 주고받도록 설계했습니다. 이를 통해 시스템의 확장성과 유지보수성을 확보했습니다.
    
    3.3 복잡한 외래키(FK) 제약 조건 오류 해결
    문제: 데이터베이스 스키마 변경 중 errno: 150 오류가 반복적으로 발생했습니다. 원인을 분석한 결과, 부모/자식 테이블 간 컬럼 타입 불일치, FK 참조 대상의 PK 또는 Unique 제약 조건 미충족 등 다양한 이유로 FK 생성이 실패했음을 확인했습니다.
    해결:
    - FK 제거 후 재설정: ALTER TABLE ... DROP FOREIGN KEY 명령어로 기존 FK를 제거한 뒤, 부모-자식 컬럼의 타입(예: BIGINT UNSIGNED NOT NULL), 부호, NULL 허용 여부를 완벽히 통일했습니다.
    - 무결성 보장: ON DELETE CASCADE, ON DELETE SET NULL과 같은 삭제 정책을 도메인에 맞게 설정하여 데이터 일관성을 유지하고, FK의 참조 대상이 PK나 Unique Index인지 재확인하는 절차를 확립했습니다.
    
    3.4 JWT 기반 인증 시스템 설계
    문제: 사용자 인증을 위한 JWT(JSON Web Token)를 Android와 백엔드 서버 간에 어떻게 안전하게 관리하고 활용할지 고민이 필요했습니다.
    해결: 로그인 성공 시 서버에서 JWT를 발급하고, Android 클라이언트는 이를 안전한 로컬 저장소에 보관하도록 했습니다. 이후 모든 API 요청 시 HTTP 헤더의 Authorization: Bearer <token> 필드에 JWT를 포함시켜 보내도록 구현했습니다. 백엔드에서는 Spring Security 필터를 활용하여 요청이 컨트롤러에 도달하기 전 JWT의 유효성을 자동으로 검증하는 로직을 추가하여, 보안성과 개발 효율성을 모두 높였습니다.
    """
)

# 4. 성과 및 기여
pdf.chapter_title(4, "성과 및 기여")
pdf.chapter_body(
    """이 프로젝트를 통해 복잡한 시스템 아키텍처를 설계하고, 이기종 간 연동 및 데이터베이스 무결성 관리 등 실무에서 필요한 백엔드 역량을 강화할 수 있었습니다. 특히, 단순한 기능 구현을 넘어 기술적 문제를 직접 분석하고 해결하는 과정을 통해 문제 해결 능력을 증명할 수 있는 좋은 경험이 되었습니다.
    """
)

# 저장
pdf_file_name = "backend_portfolio_final.pdf"
pdf.output(pdf_file_name)
print(f"PDF가 {pdf_file_name}로 생성되었습니다.")
