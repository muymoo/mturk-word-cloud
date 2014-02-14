Mechanical Turk Word Cloud
============
## Setup
### Prerequesites

1. Java AWS SDK Mechanical Turk Setup per these instructions: http://aws.amazon.com/code/Amazon-Mechanical-Turk/695. Try running the helloworld example so you know that your good to go.
2. Play 2.2.1: http://www.playframework.com/download. Just download, unzip, and add the play folder to your PATH

### Add AWS Keys
Before you run the application, be sure to update the `conf/mturk.properties` file with your `access_key` and `secret_key`

### Running
From the root directory, type `play run` and navigate to `http://localhost:9000` in your browser. For more information on developing with Play, see: http://www.playframework.com/documentation/2.2.x/Home
