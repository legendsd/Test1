import java.io.*;
import java.net.URL;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class CourseTask_Download {

    public static void main(String[] args) {
        try {
            // 1. 读取网络上的文件并解析获取图片路径
            URL url = new URL("http://10.122.7.154/javaweb/data/images-url.txt");
            BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()));

            List<String> imageUrls = new ArrayList<>();
            String line;
            while ((line = reader.readLine()) != null) {
                imageUrls.add(line);
            }
            reader.close();

            // 2. 在本地C盘根目录创建一个文件夹images
            String localPath = "C:\\images";
            Path imagesDirectory = Paths.get(localPath);
            if (!Files.exists(imagesDirectory)) {
                Files.createDirectories(imagesDirectory);
            }

            AtomicInteger count = new AtomicInteger(1);

            // 3. 下载图片并存储到images目录
            for (String imageUrl : imageUrls) {
                URL imgUrl = new URL(imageUrl);
                String imageName = getImageNameFromURL(imageUrl);
                Path imagePath = imagesDirectory.resolve(getLocalPathFromURL(imageUrl)).resolve(imageName);

                try (InputStream in = imgUrl.openStream();
                     OutputStream out = Files.newOutputStream(imagePath)) {
                    Files.createDirectories(imagePath.getParent());
                    byte[] buffer = new byte[8192];
                    int bytesRead;
                    while ((bytesRead = in.read(buffer)) != -1) {
                        out.write(buffer, 0, bytesRead);
                    }
                    long imageSize = Files.size(imagePath);
                    System.out.println("Downloaded: " + imagePath + " Size: " + imageSize);
                } catch (IOException e) {
                    System.err.println("Downloading Failed " + imagePath);
                }
            }

            // 4. 对下载的图片按大小排序并写入到images-sorted.txt
            List<File> imageFiles = new ArrayList<>();
            Files.walk(imagesDirectory, 1).forEach(filePath -> {
                if (Files.isRegularFile(filePath)) {
                    imageFiles.add(filePath.toFile());
                }
            });

            imageFiles.sort(Comparator.comparingLong(file -> file.length()));

            Path sortedImagePath = imagesDirectory.resolve("images-sorted.txt");
            try (BufferedWriter writer = Files.newBufferedWriter(sortedImagePath)) {
                for (File imageFile : imageFiles) {
                    writer.write(imageFile.length() + " " + imageFile.toPath() + "\n");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // 从图片URL中提取图片文件名
    private static String getImageNameFromURL(String imageUrl) {
        int lastSlashIndex = imageUrl.lastIndexOf('/');
        return imageUrl.substring(lastSlashIndex + 1);
    }

    // 从图片URL中提取本地路径
    private static String getLocalPathFromURL(String imageUrl) {
        String[] parts = imageUrl.split("/");
        return String.join("\\", Arrays.copyOfRange(parts, 2, parts.length - 1));
    }
}
