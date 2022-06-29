## step2 구현 사항

기본 JDBC API로 DB 사용하는 기능 구현 -> 재사용성을 위한 리팩토링

배운 내용

1. 메서드 마다의 공통 파트는 다른 메서드로 분리
2. 메서드 마다 달라지는 파트만 집중 구현할 수 있는 뼈대 구성 by 익명 클래스, Lambda, 함수형 인터페이스
3. checked Exception과 unchecked Exception의 차이. 
4. 클래스 제너릭, 메서드 제너릭 개념
5. try () resource 구문으로 finally로 close 할 필요 없게 코드 간소화
6. 가변인자로 메서드 가독성 증가
