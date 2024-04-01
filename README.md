# Oz Racer
Entry for LibGDX game jame #28 (Theme: underworld).

Game inspired by Wipeout and Star Wars pod racer.

![screenshot-OzRacer](https://github.com/MonstrousSoftware/Oz-Racer/assets/49096535/81e3792c-8215-4ced-b036-6cb0d19070ab)

See the review here: https://youtu.be/JIteHZXv8OM?t=2037

Play the game here: https://monstrous-software.itch.io/oz-racer


A [libGDX](https://libgdx.com/) project generated with [gdx-liftoff](https://github.com/tommyettinger/gdx-liftoff).

Are you the fastest racer to cross the rock field?


Features:

    Using gdx-gltf for PBR rendering
    Cascaded shadow maps (desktop only)
    Post-processing screen shader for vignette and contrast
    Anti-aliased frame buffer (new feature in LibGDX 1.12.2-SNAPSHOT)
    Chunk based infinite terrain based on Perlin noise
    Poisson distribution of rocks and wind turbines
    Frustum culling of terrain and rocks
    Polygon based collision detection
    Dynamic field of view for feel of speed
    In-game developer menu to tweak settings (lighting, camera)
    Game Controller support for game and menus (desktop only)
Controller support is only available in the desktop version (i.e. download the jar file).

    v1.01: The desktop version has controller support, cascaded shadow maps and anti-aliasing.
    
    v1.02:  Shadows fixed in web version. Performance boost.
    
    v1.03b: Desktop version should now only require OpenGL 3.2.
    
    v1.0.4: More accurate collision detection, hot keys
    
    v1.0.5: Better collision response


## Platforms

- `core`: Main module with the application logic shared by all platforms.
- `lwjgl3`: Primary desktop platform using LWJGL3.
- `teavm`: Experimental web platform using TeaVM and WebGL.


12/03/2024:
- Last week a pull request was merged into LibGDX 1.12.2-SNAPSHOT to support anti-aliased frame buffers. This allows to use antialiasing in combination
 with post-processing shaders (e.g. vignette effect). This is only possible on desktop. The teavm version, uses FBO without AA.

17/03/2024:
 - Error messages on Intel GPU
        [LWJGL] GLFW_PLATFORM_ERROR error
        Description : WGL: Failed to make context current: The handle is invalid.
        Stacktrace  :
        org.lwjgl.glfw.GLFW.glfwMakeContextCurrent(GLFW.java:4894)

    - is related to enabling GL31 even when we are not using it (required for multi sample fbo: Framebuffer multisample requires GLES 3.1+)
    -         configuration.setOpenGLEmulation(Lwjgl3ApplicationConfiguration.GLEmulation.GL31, 4,3);
    - also occurs for GL30 but not for GL20
    - Assumed to be intel driver bug, annoying but doesn't break anything.


Breaking error on teavm version:
- java.lang.IllegalArgumentException: Comparison method violates its general contract!
- This was tracked down into an overflow condition in the default renderable sorter.
- A distance of ca. 1500 from the camera causes an overflow of the integer to 2147483647. 
- (because of putting the result of dst2() in an integer).  
Fixed by providing out own renderableSorter.  


# CREDITS:

Music:
fight.ogg via OpenGameArt.ogg, If you use this please (totally optional) credit to Ville Nousiainen and/or provide a link to http://soundcloud.com/mutkanto

Textures
https://freepbr.com/materials/eroded-smoothed-rockface/

Rust texture by Radetzki Vladimir https://www.behance.net/Radetzki_Vladimir

Rock surfaces in Blender tutorial
https://www.youtube.com/watch?v=q9GbbZKBwNg

Font:
Stalinist One (Open Font License) Designed by Alexey Maslov, Jovanny Lemonad

