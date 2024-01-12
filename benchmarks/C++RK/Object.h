#ifndef _OBJECT_H
#define _OBJECT_H

class Object {
    int refCount;
public:
    Object():refCount(0) {}
    virtual ~Object() {}
    void addRef()
    {
        refCount++;
    }
    void release()
    {
        refCount--;
        if( refCount <= 0 )
            delete this;
    }
};

// #define NO_GC

template <class T>
class Ref
{
    T* obj;
public:
    Ref(T* o = 0):obj(o)
    {
#ifndef NO_GC
        if(obj)
            obj->addRef();
#endif
    }
    Ref( const Ref& rhs ):obj(0)
    {
        *this = rhs;
    }
    ~Ref()
    {
#ifndef NO_GC
        if(obj)
            obj->release();
#endif
    }
    Ref& operator=(T* rhs )
    {
        if( obj == rhs )
            return *this;
#ifndef NO_GC
        if(obj)
            obj->release();
#endif
        obj = rhs;
#ifndef NO_GC
        if( obj )
            obj->addRef();
#endif
        return *this;
    }
    Ref& operator=(const Ref& rhs )
    {
        *this = rhs.obj;
        return *this;
    }
    T* operator->() const { return obj; }
    operator T*() const { return obj; }
    T* ptr() const { return obj; }
};

#endif // _OBJECT_H
