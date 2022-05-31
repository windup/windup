import java.awt.*;

public class JavaClassTestFile3 {
    public static void main(String[] args) {
        java.awt.peer.FontPeer peer = new java.awt.Font("Sans", 1, 1).getPeer();
        Font testFont = new Font("Sans", 1, 1);
        testFont.getFontName();
        java.awt.peer.FontPeer peer1 = testFont.getPeer();
        System.err.println(peer + " [java.awt.peer] is really internal, should not be used");
    }
}
