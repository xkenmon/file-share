## Overview

File-share is a easy to use tcp based file transfer tool.
you can use it transfer files in the same network without removable disk.

## Features

* download resuming
* block based file transfer
* connection speed limit
* shell-like cli command
* checksum of downloaded file and block
* recursive directory download

## Requirement

* Java 8

## Build

### Compile

`./mvnw clean package`

### Run as Server

`java -jar target/file-share-${version}.jar server`

### Run as Client

`java -jar target/file-share-${version}.jar client`

## Usage

### Server

```
Usage:  server [-h] [--channel-limit=<channelLimit>]
               [--global-limit=<globalLimit>] [-l=<listen>] [-p=<port>]
run as server
      --channel-limit=<channelLimit>
                          r/w speed limit per connection. e.g. 1M
      --global-limit=<globalLimit>
                          global r/w speed limit. e.g. 1G
  -h, --help              display this message
  -l, --listen=<listen>   server bind address
  -p, --port=<port>       server bind port
```

### Client

```
Usage:  client [-h] [-l=<limit>] [-p=<port>] [-s=<serverAddress>]
run as client
  -h, --help            display this message
  -l, --limit=<limit>   read/write limit, e.g. 512K, 2M, 0 to unlimited
  -p, --port=<port>     remote port
  -s, --server=<serverAddress>
                        remote ip address
```

#### Client Cli Usage

##### display remote file info

```
Usage:  ls [-chs] <path>
show file/dir information
      <path>   remote file path
  -c, -md5     calculate file checksum (only for file)
  -h, --help   display this message
  -s, -size    count directory total size (only for directory)
```

##### download remote file

```
Usage:  download [-frh] [-bs=<blockSize>] [-l=<limit>] <remotePath> <localPath>
download special file from remote server
      <remotePath>      remote file path
      <localPath>       local file path
      -bs, -blockSize=<blockSize>
                        download block size, e.g. 2M, 20M
  -f, --force           overwrite if local file exists.
  -h, --help            display this message
  -l, --limit=<limit>   download/up speed limit, e.g. 2M, 512K, 0 unlimited.
  -r, --recursive       recursive download directory.
```
