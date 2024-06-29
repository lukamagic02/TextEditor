package hr.fer.ooup.data;

public class Location {

    private int rowIndex;
    private int offset;

    public Location(int rowIndex, int offset) {
        this.rowIndex = rowIndex;
        this.offset = offset;
    }

    public int getRowIndex() {
        return rowIndex;
    }

    public void setRowIndex(int rowIndex) {
        this.rowIndex = rowIndex;
    }

    public int getOffset() {
        return offset;
    }

    public void setOffset(int offset) {
        this.offset = offset;
    }

}
