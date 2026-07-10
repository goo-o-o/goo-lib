#version 150

in vec3 Position;
uniform mat4 ProjMat;

out vec2 texCoord;

void main() {
    // map coordinates to the full clip space [-1, 1]
    vec4 outPos = ProjMat * vec4(Position.xy, 0.0, 1.0);
    gl_Position = vec4(outPos.xy, 0.2, 1.0);

    // map normalized device coordinates to full texture coordinates [0, 1]
    texCoord = outPos.xy * 0.5 + 0.5;
}