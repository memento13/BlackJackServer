import entity.CardDeck;
import entity.Dealer;
import entity.Player;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class Game extends Thread{

    Socket socket = null;
    ObjectInputStream inputStream = null;
    ObjectOutputStream outputStream = null;

    public Game(Socket socket,String passcode) throws IOException {

        System.out.println("연결중: " + socket.toString());
        this.socket = socket;
        this.inputStream = new ObjectInputStream(socket.getInputStream());
        this.outputStream = new ObjectOutputStream(socket.getOutputStream());
        boolean accessFlag = passcodeCheck(passcode, socket, inputStream, outputStream);
        if(!accessFlag) {
            System.out.println("클라이언트 접속실패");
        }
    }

    @Override
    public void run() {
        Player player = new Player(10000);
        boolean gameFlag = true;
        try {
            while (gameFlag) {
                //카드덱 생성
                CardDeck cardDeck = new CardDeck();
                //계좌지급, 배팅
                int playerBat = firstBat(socket, inputStream, outputStream, player); //메시지 전송
                //딜러카드, 플레이어 카드 2장 ,딜러 블랙잭 여부 확인
                Dealer dealer = new Dealer();
                player = new Player(player.getAccount());
                dealer.cardPop(cardDeck);
                dealer.cardPop(cardDeck);
                if (dealer.checkBlackJack()) {
                    //딜러 블랙잭
                    player.getMassage().inputCode("501");
                    player.getMassage().inputMsg("딜러 블랙잭!");
                    player.getMassage().inputDealerCards(dealer.printCards());

                    outputStream.writeUTF(player.getMassage().toString());
                    outputStream.flush();
                    player.getMassage().clearMassage();
                    break;
                } else {
                    player.cardPop(cardDeck);
                    player.cardPop(cardDeck);
                    //플레이어 블랙잭 여부 확인
                    if (player.checkBlackJack()) {
                        player.addAccount(playerBat * 2);

                        player.getMassage().inputCode("500");
                        player.getMassage().inputMsg("클라이언트 블랙잭!");
                        player.getMassage().inputClientCards(player.printCards());
                        player.getMassage().inputAccount(player.getAccount());

                        outputStream.writeUTF(player.getMassage().toString());
                        outputStream.flush();
                        player.getMassage().clearMassage();
                        break;

                    } else {
                        //새로운 루프
                        //플레이어 응답확인(히트 스탠드)
                        boolean standFlag = false;
                        boolean bustFlag = false;
                        boolean hitLoopFlag = true;
                        StringBuffer msg = null;
                        while (hitLoopFlag) {

                            player.getMassage().inputCode("800");
                            player.getMassage().inputMsg("히트? 스탠드? (히트시 hit 입력 스탠드시 stand 입력)");
                            player.getMassage().inputClientCards(player.printCards());
                            player.getMassage().inputAccount(player.getAccount());
                            player.getMassage().inputDealerFirstCard(dealer.printFirstCard());

                            outputStream.writeUTF(player.getMassage().toString());
                            outputStream.flush();
                            player.getMassage().clearMassage();

                            String answer = inputStream.readUTF();
                            if (answer.equals("hit")) {
                                player.cardPop(cardDeck); //카드 뽑음
                                if (player.checkBust()) { //플레이어 버스트

                                    bustFlag = true;
                                    //standFlag = true;
                                    hitLoopFlag = false;
                                }
                                //continue;
                            } else if (answer.equals("stand")) {
                                standFlag = true;
                                hitLoopFlag = false;
                            }
                        }
                        if (bustFlag) { //플레이어 버스트인경우

                            player.getMassage().inputCode("520");
                            player.getMassage().inputMsg("플레이어 패배");
                            player.getMassage().inputAccount(player.getAccount());
                            player.getMassage().inputClientCards(player.printCards());
                            player.getMassage().inputDealerCards(dealer.printCards());

                            outputStream.writeUTF(player.getMassage().toString());
                            outputStream.flush();
                            player.getMassage().clearMassage();

                        } else if (standFlag) { //플레이어가 스탠드를 한경우
                            dealer.cardsPop(cardDeck);
                            msg = new StringBuffer();
                            if (dealer.checkBust()) {//딜러 버스트 (플레이어 승리)
                                player.addAccount(playerBat * 2);

                                player.getMassage().inputCode("510");
                                player.getMassage().inputMsg("플레이어 승리");
                                player.getMassage().inputAccount(player.getAccount());
                                player.getMassage().inputClientCards(player.printCards());
                                player.getMassage().inputDealerCards(dealer.printCards());

                            } else {
                                if (dealer.getCardsValue() > player.getCardsValue()) { //딜러 승리

                                    player.getMassage().inputCode("520");
                                    player.getMassage().inputMsg("플레이어 패배");
                                    player.getMassage().inputAccount(player.getAccount());
                                    player.getMassage().inputClientCards(player.printCards());
                                    player.getMassage().inputDealerCards(dealer.printCards());

                                } else { //플레이어 승리
                                    player.addAccount(playerBat * 2);

                                    player.getMassage().inputCode("510");
                                    player.getMassage().inputMsg("플레이어 승리");
                                    player.getMassage().inputAccount(player.getAccount());
                                    player.getMassage().inputClientCards(player.printCards());
                                    player.getMassage().inputDealerCards(dealer.printCards());

                                }
                            }
                            //메시지 아웃풋 스트림
                            outputStream.writeUTF(player.getMassage().toString());
                            outputStream.flush();
                            player.getMassage().clearMassage();
                        }
                    }
                }
//                    게임 지속여부 물음(항상 게임이 끝나는 코드 5XX 는 게임 지속 여부를 클라이언트에서 물음)
                String answer = inputStream.readUTF();
                if (answer.equals("quit")) {
                    gameFlag = false;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }



    public static int firstBat(Socket socket, ObjectInputStream inputStream, ObjectOutputStream outputStream, Player player) throws IOException {
        boolean batPass = false;
        int batMoney = 0;
        // 계좌지급
        assignAccount(socket, inputStream, outputStream, player);
        try{
            while(!batPass){

                //배팅금액 입력받음
                batMoney = Integer.parseInt(inputStream.readUTF());

                if(batMoney> player.getAccount()){ //배팅금액이 계좌금액보다 더 큰 경우
                    player.getMassage().inputCode("301");
                    player.getMassage().inputMsg("배팅금액이 계좌금액보다 큽니다.");
                    player.getMassage().inputAccount(player.getAccount());

                    outputStream.writeUTF(player.getMassage().toString());
                    outputStream.flush();
                    player.getMassage().clearMassage();

                }else if(batMoney>500){ //배팅금액이 500보다 큰 경우
                    player.getMassage().inputCode("302");
                    player.getMassage().inputMsg("배팅금액이 500보다 큽니다");
                    player.getMassage().inputAccount(player.getAccount());

                    outputStream.writeUTF(player.getMassage().toString());
                    outputStream.flush();
                    player.getMassage().clearMassage();
                }else{ // 배팅금액이 올바른 경우

                    player.getMassage().inputCode("303");
                    player.getMassage().inputMsg("code:303;msg:"+batMoney+"이 배팅되었습니다.");

                    player.debitAccount(batMoney);
                    outputStream.writeUTF(player.getMassage().toString());
                    outputStream.flush();
                    player.getMassage().clearMassage();
                    batPass =true;
                }
            }

        }catch (Exception e){
            inputStream.close();
            outputStream.close();
            socket.close();
        }

        return batMoney;
    }

    public static void assignAccount(Socket socket, ObjectInputStream inputStream, ObjectOutputStream outputStream, Player player) throws IOException {
        try{
            player.getMassage().inputCode("300");
            player.getMassage().inputMsg("배팅하시오! (500보다 큰 금액은 안됩니다)");
            player.getMassage().inputAccount(player.getAccount());

            outputStream.writeUTF(player.getMassage().toString());
            outputStream.flush();
            player.getMassage().clearMassage();

        }catch (Exception e){
            inputStream.close();
            outputStream.close();
            socket.close();
        }
    }

    public static boolean passcodeCheck(String passcode, Socket socket, ObjectInputStream inputStream, ObjectOutputStream outputStream) throws IOException {
        boolean accessFlag = false;

        try{
            String inputPasscode = inputStream.readUTF(); //비밀번호 읽어옴
            StringBuffer msg = new StringBuffer();


            //클라이언트에서 입력한 비밀번호 검증
            if(inputPasscode.equals(passcode)){
                msg.append("code:200;msg:접속성공;");
                outputStream.writeUTF(msg.toString());
                outputStream.flush();
                accessFlag = true;
            }
            else{
                msg.append("code:400;msg:접속실패 비밀번호오류!;");
                outputStream.writeUTF(msg.toString());
                outputStream.flush();
            }
        }catch (Exception e){
//           소켓커넥션 종료
            inputStream.close();
            outputStream.close();
            socket.close();
        }finally {
            return accessFlag;
        }
    }
}
