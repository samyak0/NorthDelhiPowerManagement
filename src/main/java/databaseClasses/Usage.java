package databaseClasses;

public class Usage {

    public Usage() {
    }
    public Usage(String month, String year, int readingUnits, int usedUnits, Double billAmount, boolean paid) {
        this.month = month;
        this.year = year;
        this.readingUnits = readingUnits;
        this.usedUnits = usedUnits;
        this.billAmount = billAmount;
        this.paid = paid;
    }

    public String month;
    public String year;
    public int readingUnits;
    public int usedUnits;
    public double billAmount;
    public boolean paid;

}
