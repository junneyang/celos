addTestCase({
    name: "wordcount test case 1",
    sampleTimeStart: "2013-12-20T16:00Z",
    sampleTimeEnd: "2013-12-20T18:00Z",
    inputs: [
        hdfsInput(fixDirFromResource("test-1/input"), "/input/wordcount")
    ],
    outputs: [
        plainCompare(fixDirFromResource("test-1/output"), "/output/wordcount")
    ]
});