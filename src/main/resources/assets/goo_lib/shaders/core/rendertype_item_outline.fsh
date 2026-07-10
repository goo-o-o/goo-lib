#version 150

uniform sampler2D Sampler0;

in vec2 texCoord0;
in vec4 vertexColor;
in float vertexDistance;
uniform vec4 OutlineColor;
out vec4 fragColor;

void main() {
    float a = texture(Sampler0, texCoord0).a;
    if (a <= 0.2) {
        discard;
    }

    fragColor = OutlineColor;
}