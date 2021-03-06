package com.structurizr.documentation;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.structurizr.model.Container;
import com.structurizr.model.Model;
import com.structurizr.model.SoftwareSystem;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.URLConnection;
import java.nio.file.Files;
import java.util.Base64;
import java.util.HashSet;
import java.util.Set;

/**
 * Represents the documentation within a workspace.
 */
public class Documentation {

    private Model model;
    private Set<Section> sections = new HashSet<>();
    private Set<Image> images = new HashSet<>();

    Documentation() {
    }

    public Documentation(Model model) {
        this.model = model;
    }

    @JsonIgnore
    public Model getModel() {
        return model;
    }

    public void setModel(Model model) {
        this.model = model;
    }

    /**
     * Adds documentation content relating to a {@link SoftwareSystem} from a file.
     *
     * @param softwareSystem    the {@link SoftwareSystem} the documentation content relates to
     * @param type  the {@link Type} of the documentation content
     * @param format    the {@link Format} of the documentation content
     * @param file  a File that points to the documentation content
     * @return  a documentation {@link Section}
     * @throws IOException
     */
    public Section add(SoftwareSystem softwareSystem, Type type, Format format, File file) throws IOException {
        String content = new String(Files.readAllBytes(file.toPath()), "UTF-8");
        return add(softwareSystem, type, format, content);
    }

    /**
     * Adds documentation content related to a {@link SoftwareSystem} from a String.
     *
     * @param softwareSystem    the {@link SoftwareSystem} the documentation content relates to
     * @param type  the {@link Type} of the documentation content
     * @param format    the {@link Format} of the documentation content
     * @param content   a String containing the documentation content
     * @return  a documentation {@link Section}
     */
    public Section add(SoftwareSystem softwareSystem, Type type, Format format, String content) {
        Section section = new Section(softwareSystem, type, format, content);
        if (!sections.contains(section)) {
            this.sections.add(section);

            return section;
        } else {
            throw new IllegalArgumentException("A section of type " + type + " already exists.");
        }
    }

    /**
     * Adds documentation content related to a {@link Container} from a file.
     *
     * @param container the {@link Container} the documentation content relates to
     * @param format    the {@link Format} of the documentation content
     * @param file  a File that points to the documentation content
     * @return  a documentation {@link Section}
     * @throws IOException
     */
    public Section add(Container container, Format format, File file) throws IOException {
        String content = new String(Files.readAllBytes(file.toPath()), "UTF-8");
        return add(container, format, content);
    }

    /**
     * Adds documentation content related to a {@link Container} from a String.
     *
     * @param container the {@link Container} the documentation content relates to
     * @param format    the {@link Format} of the documentation content
     * @param content   a String containing the documentation content
     * @return  a documentation {@link Section}
     */
    public Section add(Container container, Format format, String content) {
        Section section = new Section(container, Type.Components, format, content);
        if (!sections.contains(section)) {
            this.sections.add(section);

            return section;
        } else {
            throw new IllegalArgumentException("A section of type " + section.getType() + " for " + container + " already exists.");
        }
    }

    /**
     * Gets the set of {@link Section}s.
     *
     * @return  a Set of {@link Section} objects
     */
    public Set<Section> getSections() {
        return new HashSet<>(sections);
    }

    void setSections(Set<Section> sections) {
        this.sections = sections;
    }

    /**
     * Adds png/jpg/gif images in the given directory to the workspace
     *
     * @param path  a File descriptor representing a directory on disk
     * @throws IOException
     */
    public void addImages(File path) throws IOException {
        if (path != null && path.exists() && path.isDirectory()) {
            File[] imageFiles = path.listFiles((dir, name) -> {
                name = name.toLowerCase();
                return name.endsWith(".png") || name.endsWith(".jpg") || name.endsWith(".jpeg") || name.endsWith(".gif");
            });

            for (File file : imageFiles) {
                String contentType = URLConnection.guessContentTypeFromName(file.getName());
                BufferedImage bufferedImage = ImageIO.read(file);

                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                ImageIO.write(bufferedImage, contentType.split("/")[1], bos);
                byte[] imageBytes = bos.toByteArray();

                String base64Content = Base64.getEncoder().encodeToString(imageBytes);
                Image image = new Image(file.getName(), contentType, base64Content);
                images.add(image);
            }
        }
    }

    /**
     * Gets the set of {@link Image}s in this workspace.
     *
     * @return  a Set of {@link Image} objects
     */
    public Set<Image> getImages() {
        return new HashSet<>(images);
    }

    void setImages(Set<Image> images) {
        this.images = images;
    }

    // todo
    public void hydrate() {
        for (Section section : sections) {
            section.setElement(model.getElement(section.getElementId()));
        }
    }

}
