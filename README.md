# Risk of Rain Mobs

A Minecraft mod that brings iconic enemies from Risk of Rain into Minecraft.

## 🎮 Features

- Compatible with Minecraft 1.20.1
- Mobs inspired by Risk of Rain with unique behaviors
- Mob natural spawning system
- Built with Architectury API for maximum compatibility

## 🛠️ Requirements

### Fabric
- Fabric Loader
- Architectury API (Fabric)
- Fabric API

### Forge/NeoForge
- Forge/NeoForge (compatible version)
- Architectury API (Forge/NeoForge)

## 📦 Installation

### Fabric Installation
1. Install [Fabric Loader](https://fabricmc.net/use/) for Minecraft 1.20.1
2. Download the required dependencies:
   - [Architectury API (Fabric)](https://www.curseforge.com/minecraft/mc-mods/architectury-api/files/all?version=1.20.1)
   - [Fabric API](https://www.curseforge.com/minecraft/mc-mods/fabric-api/files/all?version=1.20.1)
3. Place all downloaded .jar files in your Minecraft's `mods` folder

### Forge/NeoForge Installation
1. Install [Forge](https://files.minecraftforge.net/net/minecraftforge/forge/) or [NeoForge](https://neoforged.net/) for Minecraft 1.20.1
2. Download the required dependencies:
   - [Architectury API (Forge/NeoForge)](https://www.curseforge.com/minecraft/mc-mods/architectury-api/files/all?version=1.20.1)
3. Place all downloaded .jar files in your Minecraft's `mods` folder

## 🚀 Development

### Environment Setup

1. Clone the repository
2. Run `./gradlew genSources` (Linux/Mac) or `gradlew.bat genSources` (Windows)
3. Open the project in your preferred IDE
4. If you are redirecting Temp files with a program like RAMDisk, add this property in Run/Debug Configs > VM options: '-Djava.io.tmpdir=D:/Temp'

### Building

To build the mod:

```bash
./gradlew build  # Linux/Mac
gradlew.bat build  # Windows
```

The compiled files will be in `build/libs/`

## 🤝 Contributing

Contributions are welcome! Please read our [contribution guidelines](CONTRIBUTING.md) before submitting a pull request.

## 📄 License

This project is licensed under the MIT License. See the [LICENSE](LICENSE) file for more information.

## 📞 Contact

If you have questions or suggestions, feel free to open an [issue](https://github.com/colochampre/riskofrain_mobs/issues).

---

Developed with ❤️ by [Colochampre](https://github.com/colochampre)
