
void main(){
    //@input mat4 WorldMatrix The world matrix
    //@input vec3 pos Vertex position
    //@input float factor Time for example
    //@input float factorWeight The weight of the factor
    //@output vec3 newPos New position for the vertex

    vec4 worldCoord = WorldMatrix * vec4(pos, 1.0);
    float rand = fract(sin(factor + pos.z) * 43758.5453);
    //rand = 0.0;
    //rand = sin(worldCoord.x + worldCoord.z);
    // float timeFactor = cos(tan(factor) * factorWeight * pos.z);
    float dx = sin(pos.z + rand);
    dx = rand * 0.3;
    //dx = 0.0;//
    // dx = rand;
    newPos = vec3(pos.x + dx, pos.y, pos.z);
}