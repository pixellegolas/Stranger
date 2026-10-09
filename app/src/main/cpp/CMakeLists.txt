cmake_minimum_required(VERSION 3.18.1)
project(stranger-engine)
set(CMAKE_CXX_STANDARD 17)
set(CMAKE_CXX_STANDARD_REQUIRED ON)

if(EXISTS ${CMAKE_SOURCE_DIR}/oboe/CMakeLists.txt)
  add_subdirectory(oboe)
endif()

add_library(stranger-engine SHARED engine.cpp bass_engine.cpp)

target_include_directories(stranger-engine PRIVATE
    ${CMAKE_SOURCE_DIR}
    ${CMAKE_SOURCE_DIR}/oboe/include
    ${CMAKE_SOURCE_DIR}/dexed
    ${CMAKE_SOURCE_DIR}/dexed/msfa
    ${CMAKE_SOURCE_DIR}/surge/src/common
    ${CMAKE_SOURCE_DIR}/obxd/Engine
)

target_link_libraries(stranger-engine log android OpenSLES oboe)
