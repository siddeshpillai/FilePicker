#ifndef DOCUMENTPICKER_H
#define DOCUMENTPICKER_H

#include <QObject>

class DocumentPicker : public QObject
{
    Q_OBJECT

public:
    explicit DocumentPicker();
    ~DocumentPicker();

public slots:
    void show(void);

signals:
    void documentSelected(QString file) const;

private:
    void                *m_Delegate;
};

#endif // DOCUMENTPICKER_H
