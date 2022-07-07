import java.io.Serializable;

public class Transaction implements Serializable {
    //date on witch the transaction has been created
    private String date;
    //value of the transaction
    private double value;
    public Transaction(String s, double value){
        this.date = s;
        this.value = value;
    }
    public Transaction(){
    }

    /**
     *
     * @return double value
     */
    public double getValue() {
        return value;
    }

    /**
     *
     * @return string date
     */
    public String getDate() {
        return date;
    }
}
