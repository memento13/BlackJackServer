package entity;

import java.util.ArrayList;
import java.util.List;

public class Dealer {
    List<Card> cards;

    public Dealer() {
        this.cards = new ArrayList<>();
    }

    public boolean checkBust(){
        boolean result = false;
        int dealerValue = getCardsValue();
        if(dealerValue>21){ // 버스트인경우
            result = true;
        }
        return result;
    }

    public boolean checkBlackJack(){
        boolean result = false;
        int dealerValue = getCardsValue();
        if (dealerValue==21){
            result = true;
        }
        return result;
    }
    public void cardsPop(CardDeck cardDeck){
        int dealerValue = getCardsValue();
        while(dealerValue<17){
            cards.add(cardDeck.draw());
            dealerValue = getCardsValue();
        }
    }
    public void cardPop(CardDeck cardDeck){
        cards.add(cardDeck.draw());
    }

    public int getCardsValue() {
        int dealerValue = 0;
        for (Card card : cards) {
            dealerValue+=card.getDenomination().getPoint();
        }
        return dealerValue;
    }
    public String printCards(){
        StringBuffer sb = new StringBuffer();
        boolean first = true;
        for (Card card : cards) {
            sb.append(card).append("&");
        }
        sb.setLength(sb.length() - 1);
        return sb.toString();
    }
    public String printFirstCard(){
        return cards.get(0).toString();
    }

}
