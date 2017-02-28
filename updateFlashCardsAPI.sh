#expects the project to be in ~/git/FlashCardsAPI, pulls a new version
#then proceeds to build the application, kills the old process by pid, removes old files, unzips to the same dir
#then runs it.

cd git/FlashCardsAPI/
git pull
activator dist
cp target/universal/flashcardsapinew-1.0-SNAPSHOT.zip ~/
cd ~
kill $(cat flashcardsapinew-1.0-SNAPSHOT/RUNNING_PID) % kill old process
rm -rf flashcardsapinew-1.0-SNAPSHOT %remove old distri
unzip flashcardsapinew-1.0-SNAPSHOT.zip
./runPlayApi.sh

