import QtQuick 2.9
import QtQuick.Window 2.2

import QtQuick.Controls 2.4
import DocumentPicker 1.0

Window {
    visible: true
    width: 640
    height: 480
    title: qsTr("Hello World")

    Button {
        text: "document"

        onClicked: {
            doc.show()
        }
    }

    DocumentPicker {
        id: doc

        onDocumentSelected: {
            console.log("***FILE SELECTED***", file)
            img.source = file
        }
    }

    Image {
        id: img
        anchors.fill: parent
    }
}
