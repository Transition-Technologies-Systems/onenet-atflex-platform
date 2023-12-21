package pl.com.tt.flex.onenet.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import com.google.common.collect.Lists;

import lombok.extern.slf4j.Slf4j;
import pl.com.tt.flex.model.service.dto.file.FileDTO;

@Slf4j
public class FileUtil {

	private static final int BUFFER_SIZE = 4096;

	public static List<FileDTO> zipToFiles(byte[] zippedValue) {
		List<FileDTO> files = Lists.newArrayList();
		try (ZipInputStream zipInputStream = new ZipInputStream(new ByteArrayInputStream(zippedValue))) {
			ZipEntry entry;
			while ((entry = zipInputStream.getNextEntry()) != null) {
				ByteArrayOutputStream byteOutputStream = new ByteArrayOutputStream();
				byte[] buffer = new byte[BUFFER_SIZE];
				int bytesRead = 0;
				while ((bytesRead = zipInputStream.read(buffer)) != -1) {
					byteOutputStream.write(buffer, 0, bytesRead);
				}
				zipInputStream.closeEntry();
				byteOutputStream.close();
				files.add(new FileDTO(entry.getName(), byteOutputStream.toByteArray()));
			}
			zipInputStream.close();
			return files;
		} catch (IOException exception) {
			throw new RuntimeException("Unable to unzip passed value.", exception);
		}
	}

}
