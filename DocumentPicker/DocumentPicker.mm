#include <QtGui>
#include <QtQuick>

#import <Foundation/Foundation.h>
#import <UIKit/UIKit.h>
#import <QtGui/QtGui>
#import <MobileCoreServices/MobileCoreServices.h>

#include "DocumentPicker.h"


@interface DocumentPickerDelegate: NSObject <UINavigationControllerDelegate, UIDocumentPickerDelegate>
{
                                       DocumentPicker *m_DocumentPicker;
}
@end

@implementation DocumentPickerDelegate

- (id) initWithObject:(DocumentPicker *)documentPicker
{
    self = [super init];
    if (self) {
        m_DocumentPicker = documentPicker;
    }
    return self;
}

- (void)documentPicker:(UIDocumentPickerViewController *)controller
  didPickDocumentsAtURLs:(NSArray<NSURL *> *)urls
{
    if (controller.documentPickerMode == UIDocumentPickerModeImport)
    {
        for (NSURL* obj in urls)
        {
            NSString *myString = [obj absoluteString];
            Q_EMIT m_DocumentPicker->documentSelected(QString::fromNSString(myString));
            break;
        }
//                NSLog(@"%@",urls);
    }
}

@end

void DocumentPicker::show(void)
{
    UIView *view = reinterpret_cast<UIView*>(QGuiApplication::focusWindow()->winId());
    UIViewController *qtController = [[view window] rootViewController];
    UIDocumentPickerViewController *pickerViewController = [[[UIDocumentPickerViewController alloc] initWithDocumentTypes:@[@"public.data"] inMode:UIDocumentPickerModeImport] autorelease];

    pickerViewController.delegate = id(m_Delegate);
    [qtController presentViewController:pickerViewController animated:YES completion:nil];
}

DocumentPicker::DocumentPicker()
{
    m_Delegate = [[DocumentPickerDelegate alloc] initWithObject:this];
}

DocumentPicker::~DocumentPicker()
{
    [m_Delegate release];
}
