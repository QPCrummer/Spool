package io.github.qpcrummer.spool.gui_2.file_list;

import io.github.qpcrummer.spool.file.FileRecord;
import io.qt.core.*;
import io.qt.gui.*;
import io.qt.widgets.QStyle;
import io.qt.widgets.QStyleOptionViewItem;
import io.qt.widgets.QStyledItemDelegate;

import java.util.HashMap;
import java.util.Map;

public class FileItemDelegate extends QStyledItemDelegate {
    private static final Map<Integer, QPixmap> THUMBNAIL_CACHE = new HashMap<>();

    @Override
    public void paint(QPainter painter, QStyleOptionViewItem option, QModelIndex index) {
        painter.save();

        // Extract the record
        FileRecord file = (FileRecord) index.data(Qt.ItemDataRole.UserRole);

        // Set rectangle
        QRect rect = option.rect();

        // Draw background
        if (option.state().testFlag(QStyle.StateFlag.State_Selected)) {
            painter.setBrush(new QBrush(new QColor("#0078d7"))); // blue selection outline
            painter.setPen(Qt.PenStyle.NoPen);
            painter.drawRect(rect.adjusted(0, 0, -1, -1));
        } else if (option.state().testFlag(QStyle.StateFlag.State_MouseOver)) {
            painter.setBrush(new QBrush(new QColor("#333333"))); // hover
            painter.setPen(Qt.PenStyle.NoPen);
            painter.drawRect(rect.adjusted(0, 0, -1, -1));
        } else {
            painter.setBrush(new QBrush(new QColor("#1e1e1e"))); // default background
            painter.setPen(Qt.PenStyle.NoPen);
            painter.drawRect(rect.adjusted(0, 0, -1, -1));
        }

        // Draw border around the entry
        painter.setPen(new QPen(QColor.fromString("black"), 1));
        painter.drawRect(rect.adjusted(0, 0, -1, -1));

        // Draw text (name and author)
        painter.setPen(new QColor("white"));
        QFont font = painter.font();
        font.setBold(true);
        painter.setFont(font);
        painter.drawText(rect.adjusted(6, 6, -54, -30), file.path(), Qt.AlignmentFlag.AlignLeft.combined(Qt.AlignmentFlag.AlignVCenter));

        painter.setPen(new QColor("gray"));
        font.setBold(false);
        painter.setFont(font);
        painter.drawText(rect.adjusted(6, 30, -54, -6), file.seller(), Qt.AlignmentFlag.AlignLeft.combined(Qt.AlignmentFlag.AlignVCenter));

        // Image
        QRect imageRect = new QRect(rect.right() - 128 - 4, rect.top() + 4, 120, 120);
        String thumbPath = file.getThumbnailPath();
        if (thumbPath == null) {
            painter.setBrush(new QBrush(QColor.fromString("white")));
            painter.setPen(Qt.PenStyle.NoPen);
            painter.drawRect(imageRect);
        } else {
            painter.setRenderHint(QPainter.RenderHint.SmoothPixmapTransform, true);
            QPixmap thumb = THUMBNAIL_CACHE.get(file.id());
            if (thumb == null) {
                QPixmap original = new QPixmap(file.getThumbnailPath());
                original.setDevicePixelRatio(1.0);

                thumb = original.scaled(
                        imageRect.size(),
                        Qt.AspectRatioMode.KeepAspectRatio,
                        Qt.TransformationMode.SmoothTransformation
                );

                THUMBNAIL_CACHE.put(file.id(), thumb);
            }

            painter.drawPixmap(imageRect, thumb);
            thumb.setDevicePixelRatio(1.0);
        }

        painter.restore();
    }

    @Override
    public boolean editorEvent(
            QEvent event,
            QAbstractItemModel model,
            QStyleOptionViewItem option,
            QModelIndex index) {

        if (event.type() == QEvent.Type.MouseButtonRelease) {
            QMouseEvent mouseEvent = (QMouseEvent) event;

            FileRecord file = (FileRecord) index.data(Qt.ItemDataRole.UserRole);

            QRect rect = option.rect();

            int padding = 12;
            int imageSize = 96;

            QRect imageRect = new QRect(
                    rect.right() - imageSize - padding,
                    rect.top() + (rect.height() - imageSize) / 2,
                    imageSize,
                    imageSize
            );

            if (imageRect.contains(mouseEvent.pos())) {
                // Load full image
                String thumbPath = file.getThumbnailPath();
                if (thumbPath != null) {
                    QPixmap fullImage = new QPixmap(thumbPath);

                    ImagePopup popup = new ImagePopup(
                            fullImage,
                            option.widget()
                    );
                    popup.exec();
                }
                return true;
            }
        }

        return false;
    }


    @Override
    public QSize sizeHint(QStyleOptionViewItem option, QModelIndex index) {
        return new QSize(300, 128); // row height
    }

    /**
     * Removes a cached thumbnail when the file is deleted
     * @param id {@link FileRecord#id()}
     */
    public static void removeThumbnailFromCache(int id) {
        THUMBNAIL_CACHE.remove(id);
    }
}

