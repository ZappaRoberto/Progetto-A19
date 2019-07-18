package card_management;

public class Card implements Comparable<Card> {
    private Seme seme;
    private int valore;
    private int points;
    private Valore value;
    private String cardId;


     Card(Seme seme, int valore) {
        this.seme = seme;
        this.valore = valore;
        pointsCalculator();
        createId();
        addValue();
    }

    public Card(String cardId) {
        this.cardId = cardId;
        this.valore = Integer.parseInt(cardId.replaceAll("[\\D]", ""));//(cardId.charAt(cardId.length()-1));
        for (Seme s:Seme.values()) {
            if(cardId.contains(s.toString())) {
                this.seme = s;
            }
        }
        pointsCalculator();
        addValue();
    }
    private void addValue() {
        switch (this.valore) {
            case 1:
                value = Valore.ACE;
                break;
            case 2:
                value = Valore.TWO;
                break;
            case 3:
                value = Valore.THREE;
                break;
            case 4:
                value = Valore.FOUR;
                break;
            case 5:
                value = Valore.FIVE;
                break;
            case 6:
                value = Valore.SIX;
                break;
            case 7:
                value = Valore.SEVEN;
                break;
            case 8:
                value = Valore.EIGHT;
                break;
            case 9:
                value = Valore.NINE;
                break;
            case 10:
                value = Valore.TEN;
                break;
        }
    }

    private void pointsCalculator() {
        switch(this.valore) {
            case 1:
                points = 11;
                break;
            case 3:
                points = 10;
                break;
            case 10:
                points = 4;
                break;
            case 9:
                points = 3;
                break;
            case 8:
                points = 2;
                break;
            default:
                points = 0;
                break;
        }
    }

    void setValue(Valore value) {
        this.value = value;
    }
    int getValor() {
        return valore;
    }

    public Seme getSeme() {
        return seme;
    }

    private void createId() {
        this.cardId = seme.toString() + valore;
    }

    public String getCardId() {
        return cardId;
    }

    public int getPoints() {
        return points;
    }

    private Valore getValue() {
        return value;
    }
     boolean isGreaterStessoSeme(Card card) {
        return this.value.ordinal() > card.getValue().ordinal();
    }


    @Override
    public int compareTo(Card o) {
        if (this.seme.ordinal() == o.getSeme().ordinal()) {
            if (this.value.ordinal() > o.getValue().ordinal()) {
                return 1;
            }
            else {
                return -1;
            }
        }else if(this.seme.ordinal() > o.getSeme().ordinal()) {
            return 1;
        }
        else {
            return -1;
        }
    }
}
