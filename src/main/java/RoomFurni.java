public class RoomFurni {
    public int id;
    public int typeId;
    public int median;
    public int quantity;

    public RoomFurni(int id, int typeId, int median, int quantity) {
        this.id = id;
        this.typeId = typeId;
        this.median = median;
        this.quantity = quantity;
    }

    public int getId() {
        return id;
    }

    @Override
    public String toString() {
        return "RoomFurni{" +
                "id=" + id +
                ", typeId=" + typeId +
                ", median=" + median +
                ", quantity=" + quantity +
                '}';
    }

    public int getTypeId() {
        return typeId;
    }


    public int getQuantity() {
        return quantity;
    }

    public void setMedian(int median) {
        this.median = median;
    }

    public int getMedian() {
        return median;
    }
}
