package entity;

import java.util.ArrayList;
import java.util.List;

public class Player {
    private List<Card> cards;
    private int account;

    public Player(int account) {
        this.cards = new ArrayList<>();
        this.account = account;
    }

    public void cardPop(CardDeck cardDeck){
        cards.add(cardDeck.draw());
    }

    public int getCardsValue() {
        int cardsValue = 0;
        for (Card card : cards) {
            cardsValue+=card.getDenomination().getPoint();
        }
        return cardsValue;
    }

    public boolean checkBust(){
        boolean result = false;
        int cardsValue = getCardsValue();
        if(cardsValue>21){ // 버스트인경우
            result = true;
        }
        return result;
    }

    public boolean checkBlackJack(){
        boolean result = false;
        int cardsValue = getCardsValue();
        if (cardsValue==21){
            result = true;
        }
        return result;
    }

    public String printCards(){
        StringBuffer sb = new StringBuffer();
        for (Card card : cards) {
            sb.append(card).append("&");
        }
        sb.setLength(sb.length() - 1);
        return sb.toString();
    }



    public List<Card> getCards() {
        return cards;
    }

    public void setCards(List<Card> cards) {
        this.cards = cards;
    }

    public int getAccount() {
        return account;
    }

    public void debitAccount(int money){
        account = account- money;
    }

    public void addAccount(int money){
        account = account+ money;
    }

    public void setAccount(int account) {
        this.account = account;
    }
}
