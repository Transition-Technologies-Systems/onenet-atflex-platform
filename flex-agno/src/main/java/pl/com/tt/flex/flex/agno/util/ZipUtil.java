package pl.com.tt.flex.flex.agno.util;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import pl.com.tt.flex.model.service.dto.file.FileDTO;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class ZipUtil {
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

	public static byte[] filesToZip(Iterable<FileDTO> files) {
		Set<String> fileNames = Sets.newHashSet();
		ByteArrayOutputStream byteOutputStream = new ByteArrayOutputStream();
		try (ZipOutputStream zipOutputStream = new ZipOutputStream(byteOutputStream)) {
			for (FileDTO dto : files) {
				ZipEntry entry = new ZipEntry(getFilenameWithoutDuplicate(fileNames, dto));
				entry.setSize(dto.getBytesData().length);
				zipOutputStream.putNextEntry(entry);
				zipOutputStream.write(dto.getBytesData());
				zipOutputStream.closeEntry();
			}
        } catch (IOException exception) {
			throw new RuntimeException("Unable to zip passed value.", exception);
		}
		return byteOutputStream.toByteArray();
	}

	private static String getFilenameWithoutDuplicate(Set<String> fileNames, FileDTO dto) {
		int i = 1;
		String prefix = "";
		while (fileNames.contains(prefix + dto.getFileName())) {
			prefix = i + "_";
			i++;
		}
		String filename = prefix + dto.getFileName();
		fileNames.add(filename);
		return filename;
	}
}
