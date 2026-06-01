#version 150

in vec4 vertexColor;
in vec2 vertexUV; // This receives the 0.0 to 1.0 UV coords we passed in Java!

out vec4 fragColor;

void main() {
    // Example: Create a horizontal gradient fade from center out to the padding edges
    float gradient = smoothstep(0.0, 0.5, vertexUV.x) * smoothstep(1.0, 0.5, vertexUV.x);

    // Mix the base vertexColor with your custom shader math
    fragColor = vec4(vertexColor.rgb, vertexColor.a * gradient);
}