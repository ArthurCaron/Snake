import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.*;
import java.nio.file.*;

public class MakeIcons {
    static BufferedImage scale(BufferedImage src, int size) {
        BufferedImage out = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        for (int y = 0; y < size; y++)
            for (int x = 0; x < size; x++)
                out.setRGB(x, y, src.getRGB(x * src.getWidth() / size, y * src.getHeight() / size));
        return out;
    }
    static byte[] png(BufferedImage img) throws IOException {
        ByteArrayOutputStream b = new ByteArrayOutputStream();
        ImageIO.write(img, "png", b);
        return b.toByteArray();
    }
    static void ico(BufferedImage src, int[] sizes, Path out) throws IOException {
        byte[][] pngs = new byte[sizes.length][];
        for (int i = 0; i < sizes.length; i++) pngs[i] = png(scale(src, sizes[i]));
        int offset = 6 + 16 * sizes.length;
        ByteBuffer bb = ByteBuffer.allocate(offset + java.util.Arrays.stream(pngs).mapToInt(p -> p.length).sum()).order(ByteOrder.LITTLE_ENDIAN);
        bb.putShort((short) 0).putShort((short) 1).putShort((short) sizes.length);
        for (int i = 0; i < sizes.length; i++) {
            int s = sizes[i];
            bb.put((byte) (s >= 256 ? 0 : s)).put((byte) (s >= 256 ? 0 : s)).put((byte) 0).put((byte) 0);
            bb.putShort((short) 1).putShort((short) 32).putInt(pngs[i].length).putInt(offset);
            offset += pngs[i].length;
        }
        for (byte[] p : pngs) bb.put(p);
        Files.write(out, bb.array());
    }
    static void icns(BufferedImage src, Path out) throws IOException {
        byte[] p128 = png(scale(src, 128)), p256 = png(scale(src, 256)), p512 = png(scale(src, 512));
        int total = 8 + (8 + p128.length) + (8 + p256.length) + (8 + p512.length);
        ByteBuffer bb = ByteBuffer.allocate(total).order(ByteOrder.BIG_ENDIAN);
        bb.put("icns".getBytes()).putInt(total);
        bb.put("ic07".getBytes()).putInt(8 + p128.length).put(p128);
        bb.put("ic08".getBytes()).putInt(8 + p256.length).put(p256);
        bb.put("ic09".getBytes()).putInt(8 + p512.length).put(p512);
        Files.write(out, bb.array());
    }
    public static void main(String[] a) throws IOException {
        BufferedImage src = ImageIO.read(new File(a[0]));
        Path repo = Paths.get(a[1]);
        for (int s : new int[]{128, 64, 32, 16})
            ImageIO.write(scale(src, s), "png", repo.resolve("lwjgl3/src/main/resources/icon" + s + ".png").toFile());
        ImageIO.write(scale(src, 256), "png", repo.resolve("lwjgl3/icons/logo.png").toFile());
        ico(src, new int[]{16, 32, 48, 256}, repo.resolve("lwjgl3/icons/logo.ico"));
        icns(src, repo.resolve("lwjgl3/icons/logo.icns"));
        ico(src, new int[]{16, 32, 48}, repo.resolve("sourceAssets/web/favicon.ico"));
    }
}
