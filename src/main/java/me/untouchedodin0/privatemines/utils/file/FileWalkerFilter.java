package me.untouchedodin0.privatemines.utils.file;

import com.google.common.collect.Lists;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.List;

public class FileWalkerFilter implements FileVisitor<Path> {
    private final String search;
    private final List<Path> matches = Lists.newArrayList();

    public FileWalkerFilter(String search) {
        this.search = search.toLowerCase();
    }

    @Override
    public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
        return null;
    }

    @Override
    public FileVisitResult visitFile(Path path, BasicFileAttributes attrs) throws IOException {
        String formattedFileName = path.toFile().getName().toString();
        if (formattedFileName.contains(search)) matches.add(path);
        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
        return null;
    }

    @Override
    public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
        return null;
    }

    public List<Path> getMatches() {
        return matches;
    }
}
