// rendertype_text.vsh
#version 150

in vec3 Position;
in vec4 Color;
in vec2 UV0;

uniform mat4 ModelViewMat;
uniform mat4 ProjMat;

out vec4 vertexColor;
out vec2 texCoord;
out vec3 glyphT0;
out vec3 glyphT1;
out vec3 glyphT2;
out vec3 glyphT3;

void main() {
    gl_Position = ProjMat * ModelViewMat * vec4(Position, 1.0);
    vertexColor = Color;
    texCoord    = UV0;

    glyphT0 = vec3(0.0);
    glyphT1 = vec3(0.0);
    glyphT2 = vec3(0.0);
    glyphT3 = vec3(0.0);
    int vid = gl_VertexID % 4;
    if (vid == 0) glyphT0 = vec3(UV0, 1.0);
    if (vid == 1) glyphT2 = vec3(UV0, 1.0);
    if (vid == 2) glyphT1 = vec3(UV0, 1.0);
    if (vid == 3) glyphT3 = vec3(UV0, 1.0);
}