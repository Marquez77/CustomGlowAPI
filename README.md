# CustomGlowAPI
- [ProtocolLib](https://github.com/dmulloy2/ProtocolLib/)을 연동하여 클라이언트 단위로 엔티티를 발광시킬 수 있는 API 입니다.
- 플러그인 적용 시 [ProtocolLib](https://github.com/dmulloy2/ProtocolLib/) 플러그인이 필수로 요구됩니다.
- 해당 플러그인은 `1.19` 버전 기준으로 개발되어 하위 버전에서는 작동하지 않을 수 있습니다.
---
> [GlowAPI](https://github.com/InventivetalentDev/GlowAPI) 를 참고하였으며, [GlowAPI](https://github.com/InventivetalentDev/GlowAPI) 의 이하 문제들을 해결하고자 개발한 플러그인입니다.
> 1. [Citizens](https://github.com/CitizensDev/Citizens2) 의 NPC에게는 발광이 적용되지 않는 문제
> 2. 소환(식별)되지 않은 엔티티에게는 적용할 수 없는 문제
> 3. 플레이어 시야에서 사라지면 발광이 해제되는 문제

## Maven - How to add dependency
```xml
<repository>
    <id>jitpack.io</id>
    <url>https://jitpack.io</url>
</repository>
<dependency>
    <groupId>com.github.Marquez77</groupId>
    <artifactId>CustomGlowAPI</artifactId>
    <version>VERSION</version>
    <scope>provided</scope>
</dependency>
```

## How to use
[CustomGlowAPI](src/main/java/me/marquez/cgapi/CustomGlowAPI.java) 클래스 하나로 모든 기능을 사용할 수 있습니다.
```java
CustomGlowAPI.isGlowing(UUID uuid, Player receiver) :: boolean
CustomGlowAPI.setGlowing(UUID uuid, ChatColor color, Player receiver)
CustomGlowAPI.setGlowing(UUID uuid, Player receiver) //흰색으로 발광
CustomGlowAPI.unsetGlowing(UUID uuid, Player receiver)

CustomGlowAPI.isGlowing(Entity entity, Player receiver) :: boolean
CustomGlowAPI.isGlowing(int entityId, Player receiver) :: boolean
CustomGlowAPI.getGlowingColor(Entity entity, Player receiver) :: ChatColor
CustomGlowAPI.getGlowingColor(int entityId, Player receiver) :: ChatColor
CustomGlowAPI.setGlowing(Entity entity, ChatColor color, Player receiver)
CustomGlowAPI.setGlowing(Entity entity, Player receiver) //흰색으로 발광
CustomGlowAPI.unsetGlowing(Entity entity, Player receiver)
```

## Dependency
```xml
<dependency>
    <groupId>com.comphenix.protocol</groupId>
    <artifactId>ProtocolLib</artifactId>
    <version>${protocollib.version}</version>
    <scope>provided</scope>
</dependency>
```