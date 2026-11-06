module Demo
{
    sequence<byte> ByteSeq;

    interface Callback
    {
        void notifyCallback(string msg);
    }

    interface ImageManager
    {
        void subscribe(int busId, Callback* callback);

        void sendImage(int busId, string tag, string timestamp, ByteSeq image);
    }
}