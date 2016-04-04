# -*- coding: utf-8 -*-

import re, sys, copy
from collections import defaultdict

class IO:
    def __init__(self, fn):
        f = open(fn, "r")
        self.f = f
        self.sents = []
        self.sent_id = []

    def setSents(self):
        sent = []
        for line in self.f:
            line = line.rstrip()
            if "*" == line[0]:
                tmp = line.split()
                b_id = int(tmp[1])
                head = int(tmp[2][:-1])
                if tmp[2][-1] == "P":
                    parallel = True
                else:
                    parallel = False
                if b_id != 0:
                    bunsetsu.setID()
                    bunsetsu.setBunsetsuHead()
                    sent.append(bunsetsu)
                bunsetsu = Bunsetsu(b_id, head, parallel)
            elif "EOS" == line:
                bunsetsu.setID()
                bunsetsu.setBunsetsuHead()
                sent.append(bunsetsu)
                for b in sent:
                    if b.index == b.head:
                        sent = []
                        continue
                self.sents.append(sent)
                sent = []
            elif "#" != line[0]:
                word = line.split()
                bunsetsu.word.append(Item(word))
            else:
                line = line.split()
                line = line[1].split(":")
                line = line[1].split("-")
                self.sent_id.append(line[0])

    def setChild(self):
        for sent in self.sents:
            for chunk in sent:
                for child in sent:
                    if child.head == chunk.index:
                        chunk.child.append(child.index)


    def setPasDeps(self):
        z = 0
        t = 0
        for s_index, sent in enumerate(self.sents):
            for bunsetsu in sent:
                for w in bunsetsu.word:
                    if 'type="pred"' in w.pas:
                        bunsetsu.pred = True
                    else:
                        continue
                                                    
                    for p in w.pas:
                        if "ga=" == p[:3]:
                            if p[4:8] == "exog":
                                bunsetsu.zeroGa.append(-1)
                                continue

                            tmp = re.match("\d+", p[4:])
                            if tmp != None:
                                flag = True
                                ids = int(tmp.group())
                                for b in sent:
                                    if ids in b.ID and b.index != bunsetsu.index:
                                        if b.head == bunsetsu.index or bunsetsu.head == b.index:
                                            bunsetsu.Ga_Head.append(b.index)
                                        else:
                                            bunsetsu.add_Ga_Head.append(b.index)
                                        flag = False
                                        break
                                if flag:
                                    count = 1
                                    while flag:
                                        tmp_s_index = s_index - count
                                        if tmp_s_index < 0 or self.sent_id[s_index] != self.sent_id[tmp_s_index]:
                                            break
                                        for b in self.sents[tmp_s_index]:
                                            if ids in b.ID and b.index != bunsetsu.index:
                                                bunsetsu.zeroGa.append(str(count) + "_" + str(b.index))
                                                flag = False
                                                if count == 1:
                                                    z += 1
                                                t += 1
                                                break
                                        count += 1

                    for p in w.pas:
                        if "o=" == p[:2]:
                            tmp = re.match("\d+", p[3:])
                            if tmp != None:
                                flag = True
                                ids = int(tmp.group())
                                for b in sent:
                                    if ids in b.ID and b.index != bunsetsu.index:
                                        if b.head == bunsetsu.index or bunsetsu.head == b.index:
                                            bunsetsu.O_Head.append(b.index)
                                        else:
                                            bunsetsu.add_O_Head.append(b.index)
                                        flag = False
                                        break
                                if flag:
                                    count = 1
                                    while flag:
                                        tmp_s_index = s_index - count
                                        if tmp_s_index < 0 or self.sent_id[s_index] != self.sent_id[tmp_s_index]:
                                            break
                                        for b in self.sents[tmp_s_index]:
                                            if ids in b.ID and b.index != bunsetsu.index:
                                                bunsetsu.zeroO.append(str(count) + "_" + str(b.index))
                                                flag = False
                                                if count == 1:
                                                    z += 1
                                                t += 1
                                                break
                                        count += 1

                    for p in w.pas:
                        if "ni=" == p[:3]:
                            tmp = re.match("\d+", p[4:])
                            if tmp != None:
                                flag = True
                                ids = int(tmp.group())
                                for b in sent:
                                    if ids in b.ID and b.index != bunsetsu.index:
                                        if b.head == bunsetsu.index or bunsetsu.head == b.index:
                                            bunsetsu.Ni_Head.append(b.index)
                                        else:
                                            bunsetsu.add_Ni_Head.append(b.index)
                                        flag = False
                                        break
                                if flag:
                                    count = 1
                                    while flag:
                                        tmp_s_index = s_index - count
                                        if tmp_s_index < 0 or self.sent_id[s_index] != self.sent_id[tmp_s_index]:
                                            break
                                        for b in self.sents[tmp_s_index]:
                                            if ids in b.ID and b.index != bunsetsu.index:
                                                bunsetsu.zeroNi.append(str(count) + "_" + str(b.index))
                                                flag = False
                                                if count == 1:
                                                    z += 1
                                                t += 1
                                                break
                                        count += 1

#        print t
#        print z
#        print z/float(t)

    def output(self, fn):
        ga_total = 0
        o_total = 0
        ni_total = 0
        add_ga_total = 0
        add_o_total = 0
        add_ni_total = 0
        zero_ga_total = 0
        zero_o_total = 0
        zero_ni_total = 0

        out = open(fn, "w")
        for i, sent in enumerate(self.sents):
            print >> out, "# " + self.sent_id[i]
            for b in sent:
                Ga_Head = self.convertFormat(b.Ga_Head)
                add_Ga_Head = self.convertFormat(b.add_Ga_Head)
                zero_Ga = self.convertFormat(b.zeroGa)
                O_Head = self.convertFormat(b.O_Head)
                add_O_Head = self.convertFormat(b.add_O_Head)
                zero_O = self.convertFormat(b.zeroO)
                Ni_Head = self.convertFormat(b.Ni_Head)
                add_Ni_Head = self.convertFormat(b.add_Ni_Head)
                zero_Ni = self.convertFormat(b.zeroNi)
                    
                ga_total += len(b.Ga_Head)
                add_ga_total += len(b.add_Ga_Head)
                o_total += len(b.O_Head)
                add_o_total += len(b.add_O_Head)
                ni_total += len(b.Ni_Head)
                add_ni_total += len(b.add_Ni_Head)
                zero_ga_total += len(b.zeroGa)
                zero_o_total += len(b.zeroO)
                zero_ni_total += len(b.zeroNi)

                if b.pred:
                    pred = "PRED"
                else:
                    pred = "NONE"
                
                print >> out, "* %d %d %s %s %s %s %s %s %s %s %s %s" % (b.index, b.head, Ga_Head, O_Head, Ni_Head, add_Ga_Head, add_O_Head, add_Ni_Head, zero_Ga, zero_O, zero_Ni, pred)
                for w in b.word:
                    line = "%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s" % (w.form,
                                                               w.yomi,
                                                               w.r_form,
                                                               w.cpos,
                                                               w.pos,
                                                               w.l5,
                                                               w.l6,
                                                           " ".join(w.pas))

                    print >> out, line
            print >> out, "EOS"
        out.close()
        print "Ga:%d zero-Ga:%d inter-Ga:%d\nO:%d zero-O:%d inter-zero-O:%d\nNi:%d zero-Ni:%d inter-zero-Ni:%d" % (ga_total, add_ga_total, zero_ga_total, o_total, add_o_total, zero_o_total, ni_total, add_ni_total, zero_ni_total,)

    def convertFormat(self, args):
        Head = [str(arg) for arg in args]
        if len(Head) == 0:
            return "*"
        else:
            return "/".join(Head)


class Bunsetsu:
    def __init__(self, b_id, head, parallel):
        self.index = b_id
        self.head = head
        self.parallel = parallel
        self.bhead = "NONE"
        self.bfunc = "NONE"
        self.word = []
        self.ID = []
        self.Ga_Head = []
        self.O_Head = []
        self.Ni_Head = []
        self.add_Ga_Head = []
        self.add_O_Head = []
        self.add_Ni_Head = []
        self.zeroGa = []
        self.zeroO = []
        self.zeroNi = []
        self.child = []
        self.pred = False

    def setID(self):
        for w in self.word:
            for p in w.pas:
                if "id=" == p[:3]:
                    m = re.search("\d+", p)
                    self.ID.append(int(m.group()))

    def setBunsetsuHead(self):
        except_pos = ["特殊","接尾辞","助詞"]
        for w in self.word:
            if w.cpos not in except_pos:
                self.bhead = w
            elif w.cpos != "特殊":
                self.bfunc = w

class Item:
    def __init__(self, word):
        self.form = word[0]
        self.yomi = word[1]
        self.r_form = word[2]
        self.cpos = word[3]
        self.pos = word[4]
        self.l5 = word[5]
        self.l6 = word[6]
        if word[7] != "_":
            self.pas = word[7].split("/")
        else:
            self.pas = ["_"]

    def add_child(self, parent, child):
        self.childs.add((parent, child))


def test(argv):
    fn = argv[1]
    o_fn = argv[2]
    f = IO(fn)
    f.setSents()
    f.setChild()
    f.setPasDeps()
    f.output(o_fn)

if __name__ == "__main__":
    argv = sys.argv
    test(argv)
