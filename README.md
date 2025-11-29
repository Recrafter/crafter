# Crafter

Crafter is a Gradle plugin that automates Minecraft mod development across multiple loaders.
It generates configs, manages mixins, and builds everything for you — all from a simple folder structure.

[![Github Pages](https://img.shields.io/github/v/tag/Recrafter/crafter.svg?label=Github+Pages&style=for-the-badge&sort=semver)](https://recrafter.github.io/crafter) [![License: MIT](https://img.shields.io/static/v1?label=License&style=for-the-badge&message=MIT&color=yellow)](https://spdx.org/licenses/MIT)

---

## ⚙️ Setup

### `settings.gradle.kts`

Choose your mod environment (one of **four** types):

```kotlin
plugins {
  id("io.github.recrafter.recipe") version "0.2.0"
}

recipe {
  crafter {
    serverOnly()
  }
}
```

Available environment types:

| Function | Description |
|-----------|--------------|
| `clientAndServer()` | Runs both client and server logic (e.g. visuals + world rules). |
| `clientOnly()` | Runs only on the client (UI, visuals, sounds). |
| `serverOnly()` | Runs only on the server (gameplay logic, world rules). |
| `dedicatedServerOnly()` | Runs exclusively on dedicated servers (admin tools, automation). |

---

### `build.gradle.kts`

Define your mod metadata:

```kotlin
plugins {
  id("io.github.recrafter.crafter") version "0.3.0"
}

crafter {
  mod {
    name = "Example mod"
    version = "0.1.0"

    developer {
      name = "diskria"
      namespace = "io.github.diskria"
    }
  }
}
```

Optional fields (with defaults):

| Field | Default | Description |
|-------|----------|-------------|
| `id` | Derived from `name` (`snake_case`) | Internal mod ID |
| `description` | `"Minecraft Mod"` | Mod description |
| `licenseId` | `"MIT"` | License identifier |
| `homepageUrl` | — | Homepage URL |
| `runDirectoryName` | `"run"` | Game run directory |
| `javaVersion` | `21` | Target Java version |
| `developer.repoUrl` | — | GitHub repo URL |
| `developer.issuesUrl` | — | Issues tracker URL |

---

## 🧩 Project Structure

Crafter automatically detects projects named after Minecraft versions or version ranges  
(in `min--max` format, **double hyphen required**):

```
<loader>/
 └─ <min>--<max>/
     └─ <side>/
         └─ src/
             ├─ main/
             │   └─ resources/
             │       └─ cfg.accesswidener / accesstransformer.cfg
             └─ mixins/
                 └─ java/
                     └─ your/namespace/here/<mod id>/mixins/<side>/MixinClass.java
```

Each side project (`client`, `server`) contains **two source sets**:

| Source set | Purpose |
|-------------|----------|
| `main` | Kotlin/Java code related to mixins (helper logic, shared routines). |
| `mixins` | Actual Mixin classes that modify Minecraft behavior. |

Crafter automatically:
- Detects mixin classes and generates JSON configs.
- Generates entry points for each loader (empty by design).
- Prevents usage of Fabric API, Forge EventBus, or direct loader APIs — Crafter is for *direct mixin-based modification only*.

---

## 🎨 Task Grouping

Crafter organizes Gradle tasks into categories to keep the IDE panel clean:

### IDE Tasks
`IDE/Eclipse`, `IDE/IntelliJ IDEA`, `IDE/VSCode`  
→ Generate / clean IDE configs and run configurations.

### Mod Loader Tasks
For each loader Crafter automatically groups build and run tasks under:

```
Mod Loader/<Loader Name> tasks
```

Example:
```
Mod Loader/Fabric tasks
-----------------------
runClient
runServer
remapJar
validateAccessWidener
...
```

All loader tasks are cleanly separated and logically ordered.

---

## 🧠 Supported Loaders and Versions

Crafter supports all major loaders:

| Loader | Supported Version Range |
|--------|-------------------------|
| **Fabric** | 1.14.4 → Latest         |
| **Quilt** | 1.18.2 → Latest         |
| **Legacy Fabric** | 1.3.1 → 1.13.2          |
| **Babric** | Beta 1.7.3              |
| **Ornithe** | Earliest → 1.13.2       |
| **Forge** | 1.17.1 → Latest         |
| **NeoForge** | 1.20.2 → Latest         |

All builds are generated automatically — **no MDKs, no manual configs, no mapping confusion.**

---

## 🧪 Mixins-Only Philosophy

Crafter’s core principle:
> Write mixins. Forget about loader APIs.

Developers work directly with mixins that patch Minecraft logic.  
Crafter handles every layer below — configs, entry points, mappings, version differences, and build pipelines.  
Future versions may introduce a neutral entry-point interface, isolated from any specific loader.

---

## 🧰 IDE Integration — *Enderpearl* (Coming Soon)

Crafter disables auto-generated run configs in IDEA, as it will integrate with the upcoming **Enderpearl** plugin —  
a modern, interactive IDE tool for launching Minecraft directly from IntelliJ IDEA.  
(Adaptations for Eclipse/VSCode are **not** planned.)

---

## 🔍 Roadmap

### **Crafter 1.0**
Focus: finalize the current **loader-based orchestration**.

**Key goals:**
- Complete `CLI` support
  - Installable via `./gradlew installCrafterCli`
  - Create new mod projects directly:
    ```bash
    ./crafter init fabric 1.21.10
    ```
  - Automatically scaffolds correct folder trees and defaults.
- Add **`port`** command
  - Automatically finds the earliest Minecraft version.
  - Optionally launches the game for semi-automatic testing.
- Refine Gradle task grouping and internal metadata generation.

---

### **Crafter 2.0**
Focus: transition to a **mapping-based model** — the “post-loader era”.

New structure concept:

```
minecraft/
 ├─ 1.21.10/
 ├─ 1.21.1--1.21.9/
 ├─ 1.14.4--1.21.8/
 ├─ 1.7.10--1.13.2/
 ├─ 1.3.1--1.7.9/
 └─ ...
```

Developers will:
- Write all code on the **Fabric base** using **Mojang mappings + Access Wideners**.
- Crafter will:
  - Transpile `AW` → `AT` automatically.
  - Generate mods for Fabric, Quilt, Forge, and NeoForge from one unified source.
  - Eliminate visible loader folders completely.

The 2.0 architecture will make Crafter a **cross-loader, mapping-centric SDK** for Minecraft modding.

---

## 🧩 Why Crafter Exists

Over the years, Minecraft modding has fragmented into multiple competing loaders — each born from good intentions, but all chasing the same goal: to replace the previous one. NeoForge split from Forge to “fix” its legacy. Quilt forked from Fabric to “do things right”. None of them truly failed — but none unified anything either. Instead, they divided the community.

Now mod developers face a strange reality: to reach all players, they must build and maintain versions of their mods for every loader. Each with its own mappings, Gradle quirks, config formats, and forum drama. You don’t just write a mod — you navigate political borders.

Crafter was created to end that.
It doesn’t take sides. It doesn’t try to be another loader or API. Instead, it treats Minecraft itself as the platform — not the loaders around it. Crafter automates builds, mixins, and configs for every environment, so modders can focus on what actually matters: making mods, not maintaining them.

---

## 💬 Philosophy

> **Everything should be generated.  
> You just write the code.**

Crafter removes setup complexity, unifies all mod loaders, and brings Minecraft development closer to a single, clean standard — built entirely on mixins.

---

## License

This project is licensed under the [MIT License](https://spdx.org/licenses/MIT).
