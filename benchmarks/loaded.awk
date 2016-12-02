BEGIN {
    cls = 0
}
/.* --- \[/ {
    if (cls>0) {
        print "Loaded " cls " classes"
    }
    print $0
    cls = 0
}
/\[Loaded/ {
    cls = cls + 1
}
