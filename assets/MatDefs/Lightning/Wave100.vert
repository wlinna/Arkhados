
void main(){
    //@input mat4 WorldMatrix The world matrix
    //@input vec3 pos Vertex position
    //@input float lateralFactor How much the position varies laterally
    //@input float factor Time for example
    //@output vec3 newPos New position for the vertex

    vec4 worldCoord = WorldMatrix * vec4(pos, 1.0);
    float rand = fract(sin(factor + pos.z) * 43758.5453);
    float dx = sin(rand);
    dx = rand * lateralFactor;
    newPos = vec3(pos.x + dx, pos.y, pos.z);
}