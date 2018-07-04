
const readCsv = (file, keyword) => {
  // About the sed: we are replacing the messageArId columns by <removed-for-testing-purposes> since we can't be sure
  // what the ID will be when comparing with the snapshot.
  return exec(`cat ${file} | egrep '(messageArId)|(${keyword})' | sed -r "s/,[0-9]{4},/,<removed-for-testing-purposes>,/g"`).output
}

module.exports = {readCsv};
