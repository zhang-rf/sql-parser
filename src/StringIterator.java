public class StringIterator {

    private String string;
    private char boundary;
    private boolean reversedOrder;
    private int index = -1;

    public StringIterator(String string) {
        this(string, '\'', false);
    }

    public StringIterator(String string, char boundary, boolean reversedOrder) {
        this.string = string;
        this.boundary = boundary;
        this.reversedOrder = reversedOrder;
        if (reversedOrder) index = string.length();
    }

    public String next() {
        int[] indexes = new int[2];
        if (reversedOrder) {
            for (int i = 1; i >= 0; i--) {
                do {
                    index = string.lastIndexOf(boundary, --index);
                    if (index < 0) return null;
                } while (index > 0 && (string.charAt(index - 1) == '\\' && index-- > 0));
                indexes[i] = index;
            }
        } else {
            for (int i = 0; i <= 1; i++) {
                do {
                    index = string.indexOf(boundary, ++index);
                    if (index < 0) return null;
                } while (index > 0 && (string.charAt(index - 1) == '\\' && index++ < string.length()));
                indexes[i] = index;
            }
        }
        return string.substring(indexes[0], indexes[1] + 1);
    }

    public int index() {
        return index;
    }
}
