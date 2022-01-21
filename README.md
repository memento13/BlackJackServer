# BlackJackServer
## 개요
가끔 하던 블랙잭을 자체적인 프로토콜을 만들어 tcp소켓으로 통신하는 프로그램을  만들고 싶었다.
<br>
open jdk11 (JDK), InteliJ IDEA (IDE)를 사용하여 CLI 환경에서 작동하는 1:1 블랙잭 게임을 만들었다.<br>
카드의 점수계산을 구현할 때 에이스(A) 카드를 점수계산을 편의를 위해서 원래 게임의 1 또는 10이 아닌 1점으로만 제약하였다. <br>

* 서버 비밀번호 입력시 진행되는 기능과 칩스를 베팅하는 기능을 구현하였다.
* 정해진 프로토콜의 코드에 따라서 동작하는 블랙잭 알고리즘을 구현하였다.
* 쓰레드를 통해서 여러명이 하나의 서버를 통해서 플레이 할 수 있게 하였다.
<br>

## 사용방법

* open JDK 11이 필요합니다.

* Server 실행 방법<br>
BlackJackServer.jar 파일 경로의 cmd창이 필요합니다.<br>
java -jar BlackJackServer.jar 비밀번호  를 입력하여 엔터를 누른다

* Client 실행 방법<br>
BlackJackClient.jar 파일 경로의 cmd 창이 필요합니다.<br>
java -jar BlackJackClient.jar 서버의주소 비밀번호 를 입력하여 엔터를 누른다.<br>

* 게임을 진행한다.

<br>

## 결과물
두 개의 jar 파일이 결과물로 나왔으며 각각 딜러와 플레이어의 역할을 하는 server-client로 이루어져있다.<br>
서버는 정해진 알고리즘에 맞춰서 클라이언트에게 프로토콜을 넘겨주고 클라이언트는 서버로부터 받은 프로토콜을 파싱해서 코드 번호에 맞게 동작을 수행하고 서버로 다시 프로토콜에 대한 대답을 전송한다.<br><br>

### 블랙잭 구현을 위해 정의한 프로토콜을 정리한 표<br>
상태코드|설명|보유변수
---|---|---|
200|최초접속 성공| code; msg;
300|칩스 지급| code; msg; account;
301|배팅 금액이 계좌보다 큰 경우| code; msg;
302|배팅금액이 500 보다 큰 경우| code; msg;
303|배팅 성공| code; msg;
400|최초접속 실패| code; msg;
500|플레이어 블랙잭| code; msg; client_cards; account;
501|딜러 블랙잭| code; msg; dealer_cards;
510|플레이어 승리| code; msg; account; client_cards;dealer_cards;
520|플레이어 패배| code; msg; account; client_cards;dealer_cards;
800|히트, 스탠드 질의| code; msg; client_cards;dealer_cards;
<br>
### BlackJackServer 프로젝트 클래스 설명
● Card 
 - 카드를 객체화 한것이며 카드의 문양과 값(숫자,A,J,Q,K)을 객체로 가지고 있다.

● CardDeck
 - 카드덱을 객체화 한것이며 스택 자료구조로 카드들의 리스트를 가지고 있다.
 - 카드 드로우나 카드덱을 만들어 섞는 기능을 한다.

● Dealer
 - 딜러의 카드패를 객체화 한 것이다. 
 - 카드패의 카드 드로우 이후 저장, 블랙잭 여부, 버스트 여부, 카드패의 점수를    계산하는 기능을 한다.

● Player
 - 플레이어를 객체화 한 것이다. 플레이어의 카드패와 칩스를 가지고 있다.
 - 카드패의 카드 드로우 이후 저장, 블랙잭 여부, 버스트 여부, 카드패의 점수를 계산하는 기능을 
 한다.

 ● Message
 - 프로토콜을 객체화 한 것이다. 
 - 특정 변수와 변수의 내용을 메시지에 추가 할 때 함수를 통해서 쉽게 추가할 수 있다.
<br>

 ## 결론 및 소감
 프로토콜을 만들기 위해 여러 과정들을 분석후 동작들을 잘 나누어야 하며<br>
  체계적인 프로토콜 코드를 지정해주어야 한다는걸 알게되었다.<br>
또한 프로토콜을 만들 때 파싱을 위한 구분자 입력을 철저하게 관리해야하고<br>
 이러한 구분자가 없는 경우 쉽게 통신이 마비가 되어 프로그램이 작동을 중지하였다.<br>
직접 프로토콜을 만들어 보면서 특정 작업만을 위한 프로토콜이 필요하다는 것을 알게 되었다.<br>
<br>

## 참고자료
* 카드부분사용<br> 
https://github.com/jojoldu/oop-java/blob/master/src/main/java/domain/Card.java;
* 카드 덱 부분 사용 <br>
https://github.com/jojoldu/oop-java/blob/master/src/main/java/domain/CardDeck.java;
