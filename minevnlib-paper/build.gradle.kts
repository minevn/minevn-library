// Code gọi API của Paper bản mới, chỉ được nạp khi server có API tương ứng
repositories {
    maven("https://repo.papermc.io/repository/maven-public/")
}

dependencies {
    compileOnly(project(":minevnlib-bukkit"))
    // dialog API có từ Paper 1.21.7, Audience#closeDialog từ 1.21.8
    compileOnly("io.papermc.paper:paper-api:1.21.8-R0.1-SNAPSHOT")
}

java {
    // paper-api khai báo cần Java 21, module vẫn build Java 8 như các module khác
    // vì các class này chỉ chạy trên server Paper 1.21.7+ (vốn đã chạy Java 21)
    disableAutoTargetJvm()
}
