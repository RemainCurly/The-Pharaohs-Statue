#version 430

layout (location = 0) in vec3 vertPos;
layout (location = 1) in vec2 vertTex;
layout (location = 2) in vec3 vertNormal;
out vec3 varyingNormal;
out vec3 varyingLightDir;
out vec3 varyingVertPos;
out vec3 varyingHalfVector; 
out vec3 originalVertex;
out vec4 shadow_coord;
out vec2 tc;
out float wantLights;
out float isBumpy;

struct PositionalLight
{	
	vec4 ambient;
	vec4 diffuse;
	vec4 specular;
	vec3 position;
};
struct Material
{	
	vec4 ambient;
	vec4 diffuse;
	vec4 specular;
	float shininess;
};
uniform vec4 globalAmbient;
uniform PositionalLight light;
uniform Material material;
uniform mat4 mv_matrix;
uniform mat4 proj_matrix;
uniform mat4 norm_matrix;
uniform mat4 shadowMVP;
uniform float choice;
uniform float bump;

layout(binding=0) uniform sampler2D samp;
layout(binding=1) uniform sampler2DShadow shadowTex;

void main(void)
{
	varyingVertPos = (mv_matrix * vec4(vertPos,1.0)).xyz;
	varyingLightDir = light.position - varyingVertPos;
	varyingNormal = (norm_matrix * vec4(vertNormal,1.0)).xyz;
	tc = vertTex;
	originalVertex = vertPos;
	
	shadow_coord = shadowMVP * vec4(vertPos, 1.0);  //For shadow mapping 
	
	varyingHalfVector = 			//Computations same as before, plus L+V computation below
		normalize(normalize(varyingLightDir)
		+ normalize(-varyingVertPos)).xyz;

	gl_Position = proj_matrix * mv_matrix * vec4(vertPos, 1.0);
	
	wantLights = choice;
	isBumpy = bump;			//Determines if we bump map the current object 
}