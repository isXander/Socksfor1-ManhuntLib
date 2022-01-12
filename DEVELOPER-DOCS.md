# Manhunt API Documentation
[*By isXander*](https://github.com/isXander)

This mod has been made with developers in mind. It is super easy to create a
new manhunt game type with this API.

## Dependencies
This mod has a few dependencies.
* [Fabric API *](https://www.curseforge.com/minecraft/mc-mods/fabric-api)
* [Fabric Language Kotlin >= 1.7.1+kotlin.1.6.10](https://www.curseforge.com/minecraft/mc-mods/fabric-language-kotlin)
* [Kambrik >= 3.1.0](https://www.curseforge.com/minecraft/mc-mods/kambrik)

You can either make these dependencies in gradle with `modImplementation` or add them to your mods
folder in your run directory.

## Setup
* Create a new folder called `libs` and put the Manhunt jar in there. Rename the jar to `manhunt.jar`
* In gradle add a `modImplementation` dependency to that jar
(if you don't know how to do that, why are you doing commissions??).
* Sync/reload gradle.

## Usage
### Making the Game Type
```kotlin
object MyGameType : ManhuntGameType {
    override val id = "mygame"

    override fun provideTrophies(): List<(ManhuntGame) -> Unit> = listOf(
        { /* trophy trigger here! */ }
    )
}
```
```java
public class MyGameType implements ManhuntGameType {
    @Override
    public String getId() {
        return "mygame";
    }

    @Override
    public List<KFunction<ManhuntGame>> provideTrophies() {
        List<KFunction<ManhuntGame>> trophies = new ArrayList<>();
        trophies.add((game) -> {
            /* trophy trigger here! */
        });
        return trophies;
    }
}
```
This is the simplest of game types, and it doesn't get much more complicated than this.
There are a few more hooks that you can discover for yourself, all self-explanatory.

### Registering the Game Type
In your `fabric.mod.json`, there is a new type of entrypoint called `manhunt`.
Add an entrypoint that points to your game type.
(Kotlin users will need the kotlin adapter)

### Testing the Game Type
That's it! You can now test your game by using the command in game.

`/manhunt start <gameTypeId> <speedrunner> <maxTrophyRadius>`

The trophy count is determined by the amount of trophies you defined in your game type.
