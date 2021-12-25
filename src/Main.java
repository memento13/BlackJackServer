import entity.CardDeck;
import entity.Player;
import entity.Dealer;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class Main {
    public static void main(String[] args) throws IOException {

        ServerSocket serverSocket = new ServerSocket(7890);
        String passcode = args[0];
        boolean alive = true;
        while(alive){
            Socket socket = serverSocket.accept();
            System.out.println("연결중: " + socket.toString());

            ObjectInputStream inputStream = new ObjectInputStream(socket.getInputStream());
            ObjectOutputStream outputStream = new ObjectOutputStream(socket.getOutputStream());

            boolean accessFlag = passcodeCheck(serverSocket, passcode, socket, inputStream, outputStream);
            if(!accessFlag){
                System.out.println("클라이언트 접속실패");

            }else{

                Player player = new Player(10000);
                boolean gameFlag = true;
                while(gameFlag){
                    //카드덱 생성
                    CardDeck cardDeck = new CardDeck();
                    //계좌지급, 배팅
                    int playerBat = firstBat(socket, inputStream, outputStream, player); //메시지 전송
                    //딜러카드, 플레이어 카드 2장 ,딜러 블랙잭 여부 확인
                    Dealer dealer = new Dealer();
                    player = new Player(player.getAccount());
                    dealer.cardPop(cardDeck);
                    dealer.cardPop(cardDeck);
                    if(dealer.checkBlackJack()) {
                        //딜러 블랙잭
                        StringBuffer msg = new StringBuffer();
                        msg.append("code:501;msg:딜러 블랙잭!;dealer_cards:").append(dealer.printCards());
                        outputStream.writeUTF(msg.toString());
                        outputStream.flush();
                        break;
                    }else {
                        player.cardPop(cardDeck);
                        player.cardPop(cardDeck);
                        //플레이어 블랙잭 여부 확인
                        if(player.checkBlackJack()){
                            player.addAccount(playerBat*2);
                            StringBuffer msg = new StringBuffer();
                            msg.append("code:500;msg:클라이언트 블랙잭!;client_cards:").append(player.printCards()).append(";account:").append(player.getAccount());
                            outputStream.writeUTF(msg.toString());
                            outputStream.flush();
                            break;

                        }else{
                            //새로운 루프
                            //플레이어 응답확인(히트 스탠드)
                            boolean standFlag = false;
                            boolean bustFlag = false;
                            boolean hitLoopFlag = true;
                            StringBuffer msg = null;
                            while (hitLoopFlag){
                                msg = new StringBuffer();
                                msg.append("code:800;msg:히트? 스탠드? (히트시 hit 입력 스탠드시 stand 입력);")
                                        .append("client_cards:").append(player.printCards()).append(";account:").append(player.getAccount())
                                        .append(";dealer_first_card:").append(dealer.printFirstCard());
                                outputStream.writeUTF(msg.toString());
                                outputStream.flush();

                                String answer = inputStream.readUTF();
                                if(answer.equals("hit")){
                                    player.cardPop(cardDeck); //카드 뽑음
                                    if(player.checkBust()){ //플레이어 버스트

                                        bustFlag = true;
                                        //standFlag = true;
                                        hitLoopFlag = false;
                                    }
                                    //continue;
                                }
                                else if(answer.equals("stand")){
                                    standFlag = true;
                                    hitLoopFlag = false;
                                }
                            }
                            if(bustFlag){ //플레이어 버스트인경우

                                msg = new StringBuffer();
                                msg.append("code:520;msg:플레이어 패배;account:").append(player.getAccount()).append(";client_cards:").append(player.printCards()).append(";dealer_cards:").append(dealer.printCards());

                                outputStream.writeUTF(msg.toString());

                                outputStream.flush();


                            }else if(standFlag){ //플레이어가 스탠드를 한경우
                                dealer.cardsPop(cardDeck);
                                msg = new StringBuffer();
                                if(dealer.checkBust()){//딜러 버스트 (플레이어 승리)
                                    player.addAccount(playerBat*2);
                                    msg.append("code:510;msg:플레이어 승리;account:").append(player.getAccount())
                                            .append(";client_cards:").append(player.printCards()).append(";dealer_cards:").append(dealer.printCards());
                                }
                                else{
                                    if(dealer.getCardsValue()> player.getCardsValue()){ //딜러 승리
                                        msg.append("code:520;msg:플레이어 패배;account:").append(player.getAccount())
                                                .append(";client_cards:").append(player.printCards()).append(";dealer_cards:").append(dealer.printCards());
                                    }
                                    else { //플레이어 승리
                                        player.addAccount(playerBat*2);
                                        msg.append("code:510;msg:플레이어 승리;account:").append(player.getAccount())
                                                .append(";client_cards:").append(player.printCards()).append(";dealer_cards:").append(dealer.printCards());
                                    }
                                }
                                //메시지 아웃풋 스트림
                                outputStream.writeUTF(msg.toString());
                                outputStream.flush();
                            }
                        }
                    }
//                    게임 지속여부 물음(항상 게임이 끝나는 코드 5XX 는 게임 지속 여부를 클라이언트에서 물음)
                    String answer = inputStream.readUTF();
                    if(answer.equals("quit")){
                        gameFlag = false;
                    }
                }
            }


            if(inputStream != null){
                inputStream.close();
            }
            if(outputStream !=null){
                outputStream.close();
            }
            if(socket != null){
                socket.close();
            }
            alive = false;
        }

        serverSocket.close();

    }






    public static int firstBat(Socket socket, ObjectInputStream inputStream, ObjectOutputStream outputStream, Player player) throws IOException {
        boolean batPass = false;
        int batMoney = 0;
        // 계좌지급
        assignAccount(socket, inputStream, outputStream, player.getAccount());
        try{
            while(!batPass){

                StringBuffer msg = new StringBuffer();

                //배팅금액 입력받음
                batMoney = Integer.parseInt(inputStream.readUTF());

                if(batMoney> player.getAccount()){ //배팅금액이 계좌금액보다 더 큰 경우
                    msg.append("code:301;msg:배팅금액이 계좌금액보다 큽니다.;account:"+ player.getAccount());
                    outputStream.writeUTF(msg.toString());
                    outputStream.flush();
                }else if(batMoney>500){ //배팅금액이 500보다 큰 경우
                    msg.append("code:302;msg:배팅금액이 500보다 큽니다.;account:"+ player.getAccount());
                    outputStream.writeUTF(msg.toString());
                    outputStream.flush();
                }else{ // 배팅금액이 올바른 경우
                    msg.append("code:303;msg:"+batMoney+"이 배팅되었습니다.");
                    player.debitAccount(batMoney);
                    outputStream.writeUTF(msg.toString());
                    outputStream.flush();
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

    public static void assignAccount(Socket socket, ObjectInputStream inputStream, ObjectOutputStream outputStream,int money) throws IOException {
        try{
            StringBuffer msg = new StringBuffer();
            msg.append("code:300;msg:배팅하시오! (500보다 큰 금액은 안됩니다);account:"+money);
            outputStream.writeUTF(msg.toString());
            outputStream.flush();

        }catch (Exception e){
            inputStream.close();
            outputStream.close();
            socket.close();
        }
    }

    public static boolean passcodeCheck(ServerSocket serverSocket, String passcode, Socket socket, ObjectInputStream inputStream, ObjectOutputStream outputStream) throws IOException {
        boolean accessFlag = false;

        try{
            String inputPasscode = inputStream.readUTF(); //비밀번호 읽어옴
            StringBuffer msg = new StringBuffer();

            //클라이언트에서 입력한 비밀번호 검증
            if(inputPasscode.equals(passcode)){
                msg.append("code:200;msg:접속성공");
                outputStream.writeUTF(msg.toString());
                outputStream.flush();
                accessFlag = true;
            }
            else{
                msg.append("code:400;msg:접속실패 비밀번호오류!");
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
