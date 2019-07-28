# BBVK
-
Status: work in progress

VK client/messenger for BlackBerry OS 4.5 devices.

## Building
- Eclipse with Ant
- [bb-ant-tools](https://github.com/jiggak/bb-ant-tools)
- BlackBerry JDE 4.5.0 or higher (net_rim_api.jar, rapc, javaloader, simulator, SignatureTool, MDS)
- Wine for launching simulator and MDS

You need to modify paths to JDE and simulator in build.xml (in all projects) and build JSON and BBVK_Options first.