cmake_minimum_required(VERSION 3.18.1)
project(stranger-engine)
set(CMAKE_CXX_STANDARD 17)
set(CMAKE_CXX_STANDARD_REQUIRED ON)

# Oboe
if(EXISTS ${CMAKE_SOURCE_DIR}/oboe/CMakeLists.txt)
  add_subdirectory(oboe)
else()
  message(WARNING "Oboe not found - run submodule init")
endif()

# Engine
add_library(stranger-engine SHARED
    engine.cpp
    bass_engine.cpp
    # Dexed core - uncomment after submodule init
    # dexed/msfa/synth.cpp
    # dexed/msfa/dx7note.cpp
    # dexed/msfa/env.cpp
    # Surge XT core
    # surge/src/common/dsp/oscillators/WavetableOscillator.cpp
    # OB-Xd
    # obxd/Engine/Filter.cpp
)

target_include_directories(stranger-engine PRIVATE
    ${CMAKE_SOURCE_DIR}
    ${CMAKE_SOURCE_DIR}/oboe/include
    ${CMAKE_SOURCE_DIR}/dexed
    ${CMAKE_SOURCE_DIR}/dexed/msfa
    ${CMAKE_SOURCE_DIR}/surge/src/common
    ${CMAKE_SOURCE_DIR}/obxd/Engine
)

target_link_libraries(stranger-engine
    log
    android
    OpenSLES
    oboe
)

if(ANDROID)
  target_compile_definitions(stranger-engine PRIVATE ANDROID_HAS_DEXED=1 ANDROID_HAS_SURGE=1 ANDROID_HAS_OBXD=1)
endif()
