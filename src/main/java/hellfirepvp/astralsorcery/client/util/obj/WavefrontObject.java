/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.client.util.obj;

import com.mojang.blaze3d.vertex.VertexFormat;

import com.mojang.blaze3d.vertex.VertexConsumer;
import hellfirepvp.astralsorcery.client.lib.RenderTypesAS;
import net.minecraft.client.Minecraft;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.MeshData;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexBuffer;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.lwjgl.opengl.GL11;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.UnaryOperator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * HellFirePvP@Admin
 * Date: 15.06.2015 / 00:06
 * on WingsExMod
 * WavefrontObject
 */
public class WavefrontObject {

    private static Pattern vertexPattern = Pattern.compile("(v( (\\-){0,1}\\d+\\.\\d+){3,4} *\\n)|(v( (\\-){0,1}\\d+\\.\\d+){3,4} *$)");
    private static Pattern vertexNormalPattern = Pattern.compile("(vn( (\\-){0,1}\\d+\\.\\d+){3,4} *\\n)|(vn( (\\-){0,1}\\d+\\.\\d+){3,4} *$)");
    private static Pattern textureCoordinatePattern = Pattern.compile("(vt( (\\-){0,1}\\d+\\.\\d+){2,3} *\\n)|(vt( (\\-){0,1}\\d+\\.\\d+){2,3} *$)");
    private static Pattern face_V_VT_VN_Pattern = Pattern.compile("(f( \\d+/\\d+/\\d+){3,4} *\\n)|(f( \\d+/\\d+/\\d+){3,4} *$)");
    private static Pattern face_V_VT_Pattern = Pattern.compile("(f( \\d+/\\d+){3,4} *\\n)|(f( \\d+/\\d+){3,4} *$)");
    private static Pattern face_V_VN_Pattern = Pattern.compile("(f( \\d+//\\d+){3,4} *\\n)|(f( \\d+//\\d+){3,4} *$)");
    private static Pattern face_V_Pattern = Pattern.compile("(f( \\d+){3,4} *\\n)|(f( \\d+){3,4} *$)");
    private static Pattern groupObjectPattern = Pattern.compile("([go]( [\\w\\d\\.]+) *\\n)|([go]( [\\w\\d\\.]+) *$)");

    private static Matcher vertexMatcher, vertexNormalMatcher, textureCoordinateMatcher;
    private static Matcher face_V_VT_VN_Matcher, face_V_VT_Matcher, face_V_VN_Matcher, face_V_Matcher;
    private static Matcher groupObjectMatcher;

    public ArrayList<Vertex> vertices = new ArrayList<Vertex>();
    public ArrayList<Vertex> vertexNormals = new ArrayList<Vertex>();
    public ArrayList<TextureCoordinate> textureCoordinates = new ArrayList<TextureCoordinate>();
    public ArrayList<GroupObject> groupObjects = new ArrayList<GroupObject>();
    private GroupObject currentGroupObject;
    private String fileName;
    private VertexFormat.Mode gLDrawingMode;

    public WavefrontObject(ResourceLocation resource) throws ModelFormatException {
        this.fileName = resource.toString();

        try {
            Resource res = Minecraft.getInstance().getResourceManager().getResource(resource)
                    .orElseThrow(() -> new ModelFormatException("Missing model resource: " + resource));
            loadObjModel(res.open());
        } catch (IOException e) {
            throw new ModelFormatException("IO Exception reading model format", e);
        }
    }

    public WavefrontObject(String filename, InputStream inputStream) throws ModelFormatException {
        this.fileName = filename;
        loadObjModel(inputStream);
    }

    public static WavefrontObject load(ResourceLocation modelLoc) throws ModelFormatException {
        return new WavefrontObject(modelLoc);
    }

    public VertexFormat.Mode getGLDrawingMode() {
        return gLDrawingMode;
    }

    private void loadObjModel(InputStream inputStream) throws ModelFormatException {
        String currentLine;
        int lineCount = 0;

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
            while ((currentLine = reader.readLine()) != null) {
                lineCount++;
                currentLine = currentLine.replaceAll("\\s+", " ").trim();

                if (!currentLine.startsWith("#") && currentLine.length() != 0) {
                    if (currentLine.startsWith("v ")) {
                        Vertex vertex = parseVertex(currentLine, lineCount);
                        if (vertex != null) {
                            vertices.add(vertex);
                        }
                    } else if (currentLine.startsWith("vn ")) {
                        Vertex vertex = parseVertexNormal(currentLine, lineCount);
                        if (vertex != null) {
                            vertexNormals.add(vertex);
                        }
                    } else if (currentLine.startsWith("vt ")) {
                        TextureCoordinate textureCoordinate = parseTextureCoordinate(currentLine, lineCount);
                        if (textureCoordinate != null) {
                            textureCoordinates.add(textureCoordinate);
                        }
                    } else if (currentLine.startsWith("f ")) {

                        if (currentGroupObject == null) {
                            currentGroupObject = new GroupObject("Default");
                        }

                        Face face = parseFace(currentLine, lineCount);

                        if (face != null) {
                            currentGroupObject.faces.add(face);
                        }
                    } else if (currentLine.startsWith("g ") | currentLine.startsWith("o ")) {
                        GroupObject group = parseGroupObject(currentLine, lineCount);

                        if (group != null) {
                            if (currentGroupObject != null) {
                                groupObjects.add(currentGroupObject);
                            }
                        }

                        currentGroupObject = group;
                    }
                }
            }

            groupObjects.add(currentGroupObject);
        } catch (IOException e) {
            throw new ModelFormatException("IO Exception reading model format", e);
        }
    }

    @OnlyIn(Dist.CLIENT)
    public VertexBuffer batch(UnaryOperator<VertexConsumer> decorator) {
        return this.batchInternal(decorator, buf -> this.render(buf));
    }

    @OnlyIn(Dist.CLIENT)
    public VertexBuffer batchOnly(UnaryOperator<VertexConsumer> decorator, String... groups) {
        return this.batchInternal(decorator, buf -> this.renderOnly(buf, groups));
    }

    @OnlyIn(Dist.CLIENT)
    public VertexBuffer batchExcept(UnaryOperator<VertexConsumer> decorator, String... excludedGroupNames) {
        return this.batchInternal(decorator, buf -> this.renderExcept(buf, excludedGroupNames));
    }

    @OnlyIn(Dist.CLIENT)
    private VertexBuffer batchInternal(UnaryOperator<VertexConsumer> decorator, Consumer<VertexConsumer> renderFn) {
        VertexBuffer vbo = new VertexBuffer(VertexBuffer.Usage.STATIC);
        if (this.getGLDrawingMode() == null) {
            return vbo;
        }
        BufferBuilder buf = Tesselator.getInstance().begin(this.getGLDrawingMode(), RenderTypesAS.POSITION_COLOR_TEX_NORMAL);
        renderFn.accept(decorator.apply(buf));
        MeshData data = buf.build();
        if (data != null) {
            vbo.bind();
            vbo.upload(data);
            VertexBuffer.unbind();
        }
        return vbo;
    }

    @OnlyIn(Dist.CLIENT)
    public void render(VertexConsumer vb) {
        for (GroupObject groupObject : groupObjects) {
            groupObject.render(vb);
        }
    }

    @OnlyIn(Dist.CLIENT)
    public void renderOnly(VertexConsumer vb, String... groups) {
        List<String> groupList = Arrays.asList(groups);
        for (GroupObject groupObject : groupObjects) {
            if (groupList.contains(groupObject.name)) {
                groupObject.render(vb);
            }
        }
    }

    @OnlyIn(Dist.CLIENT)
    public void renderExcept(VertexConsumer vb, String... excludedGroupNames) {
        boolean exclude;
        for (GroupObject groupObject : groupObjects) {
            exclude = false;
            for (String excludedGroupName : excludedGroupNames) {
                if (excludedGroupName.equalsIgnoreCase(groupObject.name)) {
                    exclude = true;
                }
            }
            if (!exclude) {
                groupObject.render(vb);
            }
        }
    }

    private Vertex parseVertex(String lineState, int lineCount) throws ModelFormatException {
        if (isValidVertexLine(lineState)) {
            lineState = lineState.substring(lineState.indexOf(" ") + 1);
            String[] tokens = lineState.split(" ");

            try {
                if (tokens.length == 2) {
                    return new Vertex(Float.parseFloat(tokens[0]), Float.parseFloat(tokens[1]));
                } else if (tokens.length == 3) {
                    return new Vertex(Float.parseFloat(tokens[0]), Float.parseFloat(tokens[1]), Float.parseFloat(tokens[2]));
                }
            } catch (NumberFormatException e) {
                throw new ModelFormatException(String.format("Number formatting error at line %d", lineCount), e);
            }
        } else {
            throw new ModelFormatException("Error parsing entry ('" + lineState + "'" + ", line " + lineCount + ") in file '" + fileName + "' - Incorrect format");
        }
        return null;
    }

    private Vertex parseVertexNormal(String lineState, int lineCount) throws ModelFormatException {
        if (isValidVertexNormalLine(lineState)) {
            lineState = lineState.substring(lineState.indexOf(" ") + 1);
            String[] tokens = lineState.split(" ");

            try {
                if (tokens.length == 3)
                    return new Vertex(Float.parseFloat(tokens[0]), Float.parseFloat(tokens[1]), Float.parseFloat(tokens[2]));
            } catch (NumberFormatException e) {
                throw new ModelFormatException(String.format("Number formatting error at line %d", lineCount), e);
            }
        } else {
            throw new ModelFormatException("Error parsing entry ('" + lineState + "'" + ", line " + lineCount + ") in file '" + fileName + "' - Incorrect format");
        }
        return null;
    }

    private TextureCoordinate parseTextureCoordinate(String lineState, int lineCount) throws ModelFormatException {
        if (isValidTextureCoordinateLine(lineState)) {
            lineState = lineState.substring(lineState.indexOf(" ") + 1);
            String[] tokens = lineState.split(" ");

            try {
                if (tokens.length == 2)
                    return new TextureCoordinate(Float.parseFloat(tokens[0]), 1 - Float.parseFloat(tokens[1]));
                else if (tokens.length == 3)
                    return new TextureCoordinate(Float.parseFloat(tokens[0]), 1 - Float.parseFloat(tokens[1]), Float.parseFloat(tokens[2]));
            } catch (NumberFormatException e) {
                throw new ModelFormatException(String.format("Number formatting error at line %d", lineCount), e);
            }
        } else {
            throw new ModelFormatException("Error parsing entry ('" + lineState + "'" + ", line " + lineCount + ") in file '" + fileName + "' - Incorrect format");
        }
        return null;
    }

    private Face parseFace(String lineState, int lineCount) throws ModelFormatException {
        if (isValidFaceLine(lineState)) {
            Face face = new Face();

            String trimmedLine = lineState.substring(lineState.indexOf(" ") + 1);
            String[] tokens = trimmedLine.split(" ");
            String[] subTokens = null;

            if (tokens.length == 3) {
                if (this.gLDrawingMode == null) {
                    this.gLDrawingMode = VertexFormat.Mode.TRIANGLES;
                } else if (this.gLDrawingMode != VertexFormat.Mode.TRIANGLES) {
                    throw new ModelFormatException("Error parsing entry ('" + lineState + "'" + ", line " + lineCount + ") in file '" + fileName + "' - Invalid number of points for face (expected 4, found " + tokens.length + ")");
                }
            } else if (tokens.length == 4) {
                if (this.gLDrawingMode == null) {
                    this.gLDrawingMode = VertexFormat.Mode.QUADS;
                } else if (this.gLDrawingMode != VertexFormat.Mode.QUADS) {
                    throw new ModelFormatException("Error parsing entry ('" + lineState + "'" + ", line " + lineCount + ") in file '" + fileName + "' - Invalid number of points for face (expected 3, found " + tokens.length + ")");
                }
            }

            // f v1/vt1/vn1 v2/vt2/vn2 v3/vt3/vn3 ...
            if (isValidFace_V_VT_VN_Line(lineState)) {
                face.vertices = new Vertex[tokens.length];
                face.textureCoordinates = new TextureCoordinate[tokens.length];
                face.vertexNormals = new Vertex[tokens.length];

                for (int i = 0; i < tokens.length; ++i) {
                    subTokens = tokens[i].split("/");

                    face.vertices[i] = vertices.get(Integer.parseInt(subTokens[0]) - 1);
                    face.textureCoordinates[i] = textureCoordinates.get(Integer.parseInt(subTokens[1]) - 1);
                    face.vertexNormals[i] = vertexNormals.get(Integer.parseInt(subTokens[2]) - 1);
                }

                face.faceNormal = face.calculateFaceNormal();
            }
            // f v1/vt1 v2/vt2 v3/vt3 ...
            else if (isValidFace_V_VT_Line(lineState)) {
                face.vertices = new Vertex[tokens.length];
                face.textureCoordinates = new TextureCoordinate[tokens.length];

                for (int i = 0; i < tokens.length; ++i) {
                    subTokens = tokens[i].split("/");

                    face.vertices[i] = vertices.get(Integer.parseInt(subTokens[0]) - 1);
                    face.textureCoordinates[i] = textureCoordinates.get(Integer.parseInt(subTokens[1]) - 1);
                }

                face.faceNormal = face.calculateFaceNormal();
            }
            // f v1//vn1 v2//vn2 v3//vn3 ...
            else if (isValidFace_V_VN_Line(lineState)) {
                face.vertices = new Vertex[tokens.length];
                face.vertexNormals = new Vertex[tokens.length];

                for (int i = 0; i < tokens.length; ++i) {
                    subTokens = tokens[i].split("//");

                    face.vertices[i] = vertices.get(Integer.parseInt(subTokens[0]) - 1);
                    face.vertexNormals[i] = vertexNormals.get(Integer.parseInt(subTokens[2]) - 1);
                }

                face.faceNormal = face.calculateFaceNormal();
            }
            // f v1 v2 v3 ...
            else if (isValidFace_V_Line(lineState)) {
                face.vertices = new Vertex[tokens.length];

                for (int i = 0; i < tokens.length; ++i) {
                    face.vertices[i] = vertices.get(Integer.parseInt(tokens[i]) - 1);
                }

                face.faceNormal = face.calculateFaceNormal();
            } else {
                throw new ModelFormatException("Error parsing entry ('" + lineState + "'" + ", line " + lineCount + ") in file '" + fileName + "' - Incorrect format");
            }
            return face;
        } else {
            throw new ModelFormatException("Error parsing entry ('" + lineState + "'" + ", line " + lineCount + ") in file '" + fileName + "' - Incorrect format");
        }
    }

    private GroupObject parseGroupObject(String lineState, int lineCount) throws ModelFormatException {
        GroupObject group = null;

        if (isValidGroupObjectLine(lineState)) {
            String trimmedLine = lineState.substring(lineState.indexOf(" ") + 1);

            if (trimmedLine.length() > 0) {
                group = new GroupObject(trimmedLine);
            }
        } else {
            throw new ModelFormatException("Error parsing entry ('" + lineState + "'" + ", line " + lineCount + ") in file '" + fileName + "' - Incorrect format");
        }

        return group;
    }

    /**
     * Verifies that the given line from the model file is a valid vertex
     *
     * @param line the line being validated
     * @return true if the line is a valid vertex, false otherwise
     */
    private static boolean isValidVertexLine(String lineState) {
        if (vertexMatcher != null) {
            vertexMatcher.reset();
        }

        vertexMatcher = vertexPattern.matcher(lineState);
        return vertexMatcher.matches();
    }

    /**
     * Verifies that the given line from the model file is a valid vertex normal
     *
     * @param line the line being validated
     * @return true if the line is a valid vertex normal, false otherwise
     */
    private static boolean isValidVertexNormalLine(String lineState) {
        if (vertexNormalMatcher != null) {
            vertexNormalMatcher.reset();
        }

        vertexNormalMatcher = vertexNormalPattern.matcher(lineState);
        return vertexNormalMatcher.matches();
    }

    /**
     * Verifies that the given line from the model file is a valid texture coordinate
     *
     * @param line the line being validated
     * @return true if the line is a valid texture coordinate, false otherwise
     */
    private static boolean isValidTextureCoordinateLine(String lineState) {
        if (textureCoordinateMatcher != null) {
            textureCoordinateMatcher.reset();
        }

        textureCoordinateMatcher = textureCoordinatePattern.matcher(lineState);
        return textureCoordinateMatcher.matches();
    }

    /**
     * Verifies that the given line from the model file is a valid face that is described by vertices, texture coordinates, and vertex normals
     *
     * @param line the line being validated
     * @return true if the line is a valid face that matches the format "f v1/vt1/vn1 ..." (with a minimum of 3 points in the face, and a maximum of 4), false otherwise
     */
    private static boolean isValidFace_V_VT_VN_Line(String lineState) {
        if (face_V_VT_VN_Matcher != null) {
            face_V_VT_VN_Matcher.reset();
        }

        face_V_VT_VN_Matcher = face_V_VT_VN_Pattern.matcher(lineState);
        return face_V_VT_VN_Matcher.matches();
    }

    /**
     * Verifies that the given line from the model file is a valid face that is described by vertices and texture coordinates
     *
     * @param line the line being validated
     * @return true if the line is a valid face that matches the format "f v1/vt1 ..." (with a minimum of 3 points in the face, and a maximum of 4), false otherwise
     */
    private static boolean isValidFace_V_VT_Line(String lineState) {
        if (face_V_VT_Matcher != null) {
            face_V_VT_Matcher.reset();
        }

        face_V_VT_Matcher = face_V_VT_Pattern.matcher(lineState);
        return face_V_VT_Matcher.matches();
    }

    /**
     * Verifies that the given line from the model file is a valid face that is described by vertices and vertex normals
     *
     * @param line the line being validated
     * @return true if the line is a valid face that matches the format "f v1//vn1 ..." (with a minimum of 3 points in the face, and a maximum of 4), false otherwise
     */
    private static boolean isValidFace_V_VN_Line(String lineState) {
        if (face_V_VN_Matcher != null) {
            face_V_VN_Matcher.reset();
        }

        face_V_VN_Matcher = face_V_VN_Pattern.matcher(lineState);
        return face_V_VN_Matcher.matches();
    }

    /**
     * Verifies that the given line from the model file is a valid face that is described by only vertices
     *
     * @param line the line being validated
     * @return true if the line is a valid face that matches the format "f v1 ..." (with a minimum of 3 points in the face, and a maximum of 4), false otherwise
     */
    private static boolean isValidFace_V_Line(String lineState) {
        if (face_V_Matcher != null) {
            face_V_Matcher.reset();
        }

        face_V_Matcher = face_V_Pattern.matcher(lineState);
        return face_V_Matcher.matches();
    }

    /**
     * Verifies that the given line from the model file is a valid face of any of the possible face formats
     *
     * @param line the line being validated
     * @return true if the line is a valid face that matches any of the valid face formats, false otherwise
     */
    private static boolean isValidFaceLine(String lineState) {
        return isValidFace_V_VT_VN_Line(lineState) || isValidFace_V_VT_Line(lineState) || isValidFace_V_VN_Line(lineState) || isValidFace_V_Line(lineState);
    }

    /**
     * Verifies that the given line from the model file is a valid group (or object)
     *
     * @param line the line being validated
     * @return true if the line is a valid group (or object), false otherwise
     */
    private static boolean isValidGroupObjectLine(String lineState) {
        if (groupObjectMatcher != null) {
            groupObjectMatcher.reset();
        }

        groupObjectMatcher = groupObjectPattern.matcher(lineState);
        return groupObjectMatcher.matches();
    }

}
