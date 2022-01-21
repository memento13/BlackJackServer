package entity;

public class Massage {
    /**
     * 메시지를 저장하고 종류별 메시지를 입력해주는 클래스
     * 저장순서 :
     * 1. 저장할 메시지 타입이 메시지에 저장되었는지 확인
     * 2. 저장이 안되어있으면 저장할 메시지 타입과 메시지 내용을 inputMassage 함수를 이용하여 저장
     * 3. 성공적으로 메시지를 저장하여서 true 반환
     */
    private StringBuffer massage = null;

    public Massage() {
        this.massage = new StringBuffer();
    }

    public StringBuffer getMassage() {
        return massage;
    }

    private boolean inputMassage(String name,String value){
        massage.append(name).append(":").append(value).append(";");
        return true;
    }

    public void clearMassage(){
        massage = new StringBuffer();
    }

    public boolean inputCode(String code){
        if(massage.toString().contains("code:")){
            return false;
        }
        inputMassage("code",code);
        return true;
    }

    public boolean inputMsg(String msg){
        if(massage.toString().contains("msg:")){
            return false;
        }
        inputMassage("msg",msg);
        return true;
    }

    public boolean inputAccount(int account){
        if(massage.toString().contains("account:")){
            return false;
        }
        inputMassage("account",Integer.toString(account));
        return true;
    }

    public boolean inputClientCards(String client_cards){
        if(massage.toString().contains("client_cards:")){
            return false;
        }
        inputMassage("client_cards",client_cards);
        return true;
    }

    public boolean inputDealerCards(String dealer_cards){
        if(massage.toString().contains("dealer_cards:")){
            return false;
        }
        inputMassage("dealer_cards",dealer_cards);
        return true;
    }

    public boolean inputDealerFirstCard(String dealer_first_card){
        if(massage.toString().contains("dealer_first_card:")){
            return false;
        }
        inputMassage("dealer_first_card",dealer_first_card);
        return true;
    }







}
