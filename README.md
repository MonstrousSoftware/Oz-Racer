# CanyonRacer

Game inspired by Wipeout and Star Wars pod racer.

A [libGDX](https://libgdx.com/) project generated with [gdx-liftoff](https://github.com/tommyettinger/gdx-liftoff).

This project was generated with a template including simple application launchers and a main class extending `Game` that sets the first screen.

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
        com.badlogic.gdx.backends.lwjgl3.Lwjgl3Window.makeCurrent(Lwjgl3Window.java:431)
        com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application.loop(Lwjgl3Application.java:189)

    - is related to enabling GL31 even when we are not using it (required for multi sample fbo: Framebuffer multisample requires GLES 3.1+)
    -         configuration.setOpenGLEmulation(Lwjgl3ApplicationConfiguration.GLEmulation.GL31, 4,3);
    - also occurs for GL30 but not for GL20
    - Assumed to be intel driver bug, annoying but doesn't break anything.



Breaking error on teavm version:
- java.lang.IllegalArgumentException: Comparison method violates its general contract!
- It seems to be within sceneManager, it does not occur if there are zero scenes in sceneManager
Even if we override the color sortet, the default renderable sorter is used for the depth pass
and this gives non transitive results (due to overflows of (int)1000*dst2()).
A distance of ca. 1500 from the camera causes an overflow of the integer to 2147483647.
- From that point on all distances are equal

[compares:] 0 : -1 vs 1
[compares:] 1 : -1 vs 1
[compares:] 2 : -1 vs 1
[compares:] 3 : 1 vs -1
[compares:] 4 : -1 vs 1

[compares:] 0 : -1 vs 1 dst:0.0 d2:0.0 integer:0
[compares:] 1 : -1 vs 1 dst:199.68153 d2:39872.715 integer:39872716
[compares:] 2 : -1 vs 1 dst:262.9471 d2:69141.19 integer:69141184
[compares:] 3 : 1 vs -1 dst:337.19852 d2:113702.836 integer:113702832
[compares:] 4 : -1 vs 1 dst:527.04626 d2:277777.78 integer:277777792

to do:
- DONE: collision with rocks
- camera avoidance of obstacles
- camera orbit control
- way points
- DONE: race time
- ai racers




Music:
fight.ogg via OpenGameArt.ogg, If you use this please (totally optional) credit to Ville Nousiainen and/or provide a link to http://soundcloud.com/mutkanto

Textures
https://freepbr.com/materials/eroded-smoothed-rockface/

Rust texture by Radetzki Vladimir https://www.behance.net/Radetzki_Vladimir

Rock surfaces in Blender tutorial
https://www.youtube.com/watch?v=q9GbbZKBwNg
